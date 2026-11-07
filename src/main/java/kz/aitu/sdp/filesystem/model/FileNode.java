package kz.aitu.sdp.filesystem.model;

import kz.aitu.sdp.filesystem.visitor.FileSystemVisitor;

import java.util.Objects;

public class FileNode implements FileSystemElement {

    private final String name;
    private final long size;
    private DirectoryNode parent;

    public FileNode(String name, long size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }
        if (size < 0) {
            throw new IllegalArgumentException("File size cannot be negative: " + size);
        }
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    @Override
    public void accept(FileSystemVisitor visitor) {
        Objects.requireNonNull(visitor, "Visitor cannot be null");
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "FileNode[name='" + name + "', size=" + size + " bytes]";
    }
}
