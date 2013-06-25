/*
 * Copyright (C) 2013  Christian Autermann, Jan Alexander Wirwahn,
 *                     Arne De Wall, Dustin Demuth, Saqib Rasheed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.car.server.rest.encoding.rdf.linker;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

import io.car.server.core.FriendService;
import io.car.server.core.GroupService;
import io.car.server.core.entities.Group;
import io.car.server.core.entities.Groups;
import io.car.server.core.entities.User;
import io.car.server.core.entities.Users;
import io.car.server.rest.encoding.rdf.RDFLinker;
import io.car.server.rest.resources.GroupsResource;
import io.car.server.rest.resources.RootResource;
import io.car.server.rest.resources.UserResource;
import io.car.server.rest.resources.UsersResource;
import io.car.server.rest.rights.AccessRights;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public class UserFOAFLinker implements RDFLinker<User> {
    public static final String PREFIX = "foaf";
    private final GroupService groupService;
    private final FriendService friendService;

    @Inject
    public UserFOAFLinker(GroupService groupService, FriendService friendService) {
        this.groupService = groupService;
        this.friendService = friendService;
    }

    @Override
    public void link(Model m, User t, AccessRights rights,
                     Provider<UriBuilder> uriBuilder) {
        UriBuilder userURIBuilder = uriBuilder.get()
                .path(RootResource.class)
                .path(RootResource.USERS)
                .path(UsersResource.USER);
        m.setNsPrefix(PREFIX, FOAF.NS);
        URI uri = userURIBuilder.build(t.getName());
        Resource p = m.createResource(uri.toASCIIString(), FOAF.Person);
        p.addLiteral(FOAF.nick, t.getName());
        if (t.hasFirstName() && rights.canSeeFirstNameOf(t)) {
            p.addLiteral(FOAF.firstName, t.getFirstName());
            p.addLiteral(FOAF.givenname, t.getFirstName());
        }
        if (t.hasLastName() && rights.canSeeLastNameOf(t)) {
            p.addLiteral(FOAF.surname, t.getLastName());
            p.addLiteral(FOAF.family_name, t.getLastName());
        }
        if (t.hasDayOfBirth() && rights.canSeeDayOfBirthOf(t)) {
            p.addLiteral(FOAF.birthday, t.getDayOfBirth());
        }
        if (t.hasUrl() && rights.canSeeUrlOf(t)) {
            p.addProperty(FOAF.homepage, m.createResource(t.getUrl()
                    .toString()));
        }
        if (t.hasGender() && rights.canSeeGenderOf(t)) {
            p.addLiteral(FOAF.gender, t.getGender().toString().toLowerCase());
        }
        if (rights.canSeeAvatarOf(t)) {
            p.addProperty(FOAF.img, m.createResource(UriBuilder.fromUri(uri)
                    .path(UserResource.AVATAR).build().toASCIIString()));
        }
        if (t.hasMail() && rights.canSeeMailOf(t)) {
            p.addLiteral(FOAF.mbox, "mailto:" + t.getMail());
        }
        if (rights.canSeeFriendsOf(t)) {
            Users friends = friendService.getFriends(t);
            for (User f : friends) {
                String friendURI = userURIBuilder
                        .build(f.getName()).toASCIIString();
                p.addProperty(FOAF.knows, m
                        .createResource(friendURI, FOAF.Person));
            }
        }
        if (rights.canSeeGroupsOf(t)) {
            Groups groups = groupService.getGroups(t, null);
            UriBuilder groupUriBuilder = uriBuilder.get()
                    .path(RootResource.class)
                    .path(RootResource.GROUPS)
                    .path(GroupsResource.GROUP);
            for (Group group : groups) {
                m.createResource(groupUriBuilder.build(group.getName())
                        .toASCIIString(), FOAF.Group)
                        .addProperty(FOAF.member, p);
            }
        }
    }
}
