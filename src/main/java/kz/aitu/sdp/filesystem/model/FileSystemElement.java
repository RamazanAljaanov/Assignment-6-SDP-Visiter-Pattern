package kz.aitu.sdp.filesystem.model;

import kz.aitu.sdp.filesystem.visitor.FileSystemVisitor;

public interface FileSystemElement {
    String getName();
    void accept(FileSystemVisitor visitor);
    DirectoryNode getParent();
    void setParent(DirectoryNode parent);
}
