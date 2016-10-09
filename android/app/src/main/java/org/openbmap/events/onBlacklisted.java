/*
	Radiobeacon - Openbmap wifi and cell logger
    Copyright (C) 2013  wish7

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openbmap.events;

import org.openbmap.services.wireless.blacklists.BlacklistReasonType;

public class onBlacklisted {

    public BlacklistReasonType reason;
    public String message;

    /**
     * Fired when free wifi found
     * @param reason Reason for blacklisting
     * @param message additional info
     */
    public onBlacklisted(final BlacklistReasonType reason, final String message) {
        this.reason = reason;
        this.message = message;
    }

}
