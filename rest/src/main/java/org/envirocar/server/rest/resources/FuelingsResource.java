/*
 * Copyright (C) 2013 The enviroCar project
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
package org.envirocar.server.rest.resources;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.envirocar.server.core.TemporalFilter;
import org.envirocar.server.core.entities.Fueling;
import org.envirocar.server.core.entities.Fuelings;
import org.envirocar.server.core.entities.User;
import org.envirocar.server.core.exception.BadRequestException;
import org.envirocar.server.core.exception.FuelingNotFoundException;
import org.envirocar.server.core.filter.FuelingFilter;
import org.envirocar.server.rest.MediaTypes;
import org.envirocar.server.rest.Schemas;
import org.envirocar.server.rest.validation.Schema;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class FuelingsResource extends AbstractResource {
    public static final String FUELING = "{id}";
    private final User user;

    @Inject
    public FuelingsResource(@Assisted User user) {
        this.user = checkNotNull(user);
    }

    @GET
    @Schema(response = Schemas.FUELINGS)
    @Produces({ MediaTypes.FUELINGS,
                MediaTypes.XML_RDF,
                MediaTypes.TURTLE,
                MediaTypes.TURTLE_ALT })
    public Fuelings getAll() throws BadRequestException {
        TemporalFilter tf = parseTemporalFilterForInstant();
        return getDataService().getFuelings(new FuelingFilter(user, tf, getPagination()));
    }

    @POST
    @Schema(request = Schemas.FUELING_CREATE)
    @Consumes({ MediaTypes.FUELING_CREATE })
    public Response create(Fueling fueling) {
        fueling.setUser(getCurrentUser());
        Fueling f = getDataService().createFueling(fueling);
        return Response.created(getUriInfo().getAbsolutePathBuilder().path(f
                .getIdentifier()).build()).build();
    }

    @GET
    @Path(FUELING)
    @Schema(response = Schemas.FUELING)
    @Produces({ MediaTypes.FUELING,
                MediaTypes.XML_RDF,
                MediaTypes.TURTLE,
                MediaTypes.TURTLE_ALT })
    public Fueling getFueling(@PathParam("id") String id)
            throws FuelingNotFoundException {
        return getDataService().getFueling(user, id);
    }
}
