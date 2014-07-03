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
package org.envirocar.server.rest.encoding;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.envirocar.server.core.exception.TrackTooLongException;
import org.envirocar.server.rest.rights.AccessRights;

/**
 * TODO JavaDoc
 *
 * @author Benjamin Pross
 */
public interface ShapefileTrackEncoder<T> {
    File encodeShapefile(T t, AccessRights rights, MediaType mt) throws TrackTooLongException;
}
