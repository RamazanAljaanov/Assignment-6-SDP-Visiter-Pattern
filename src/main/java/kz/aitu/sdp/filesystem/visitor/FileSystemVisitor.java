package kz.aitu.sdp.filesystem.visitor;

import kz.aitu.sdp.filesystem.model.DirectoryNode;
import kz.aitu.sdp.filesystem.model.FileNode;

public interface FileSystemVisitor {
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}
