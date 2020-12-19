/*
    Copyrite 2020 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp;

import java.io.File;
import java.io.IOException;

/**
 * Wrap S3 metadata in a File interface.
 * 
 * @author Will Winder
 */
public class S3VirtualFile extends File {

    private static final long serialVersionUID = -1752685357864733168L;
    private final boolean isDir;
    private final long length;
    private long lastModified = 0;

    protected S3VirtualFile(final File file, long length) {
        this(file.toString(), length);
    }

    protected S3VirtualFile(String pathname, long length) {
        super(pathname);
        isDir = pathname.endsWith("/");
        this.length = length;
    }

    protected S3VirtualFile(String parent, String child, long length) {
        super(parent, child);
        isDir = child.endsWith("/");
        this.length = length;
    }

    protected S3VirtualFile(File parent, String child, long length) {
        super(parent, child);
        isDir = child.endsWith("/");
        this.length = length;
    }

    @Override
    public String getName() {
        int idx = this.toString().lastIndexOf("/");
        if (idx == -1) return this.toString();
        return this.toString().substring(idx + 1);
    }

    @Override
    public boolean setLastModified(long t) {
        this.lastModified = t;
        return true;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return isDir;
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }

    @Override
    public File getAbsoluteFile() {
        return this;
    }

    @Override
    public File getParentFile() {
        final int lastIndex = this.toString().lastIndexOf('/');

        if (lastIndex == -1) return null;

        String parent = this.toString().substring(0, lastIndex + 1);

        return new S3VirtualFile(parent, 1);
    }

}

