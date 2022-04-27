package com.github.filipmalczak.storyteller.impl.storage.files;

import com.github.filipmalczak.storyteller.api.tree.task.id.IdSerializer;
import com.github.filipmalczak.storyteller.impl.storage.files.indexing.Modification;
import com.github.filipmalczak.storyteller.impl.storage.files.indexing.ModificationEvent;
import com.github.filipmalczak.storyteller.impl.storage.files.indexing.ModificationIndex;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.valid4j.Assertive.require;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class FileIndex<Id extends Comparable<Id>> {
    @NonNull Path filesRoot;
    @NonNull ModificationIndex<Id> modificationIndex;
    @NonNull IdSerializer<Id> idSerializer;

    FileIndex(@NonNull Path filesRoot, @NonNull ModificationIndex<Id> modificationIndex, @NonNull IdSerializer<Id> idSerializer) {
        require(filesRoot.isAbsolute(), "Root of file storage must be absolute path");
        this.filesRoot = filesRoot;
        this.modificationIndex = modificationIndex;
        this.idSerializer = idSerializer;
    }

    private Path resolvePath(Id scope){
        return filesRoot.resolve(idSerializer.toString(scope));
    }

    private File resolve(Id scope){
        return resolvePath(scope).toFile();
    }

    private File resolve(Id scope, String path){
        return resolvePath(scope).resolve(path).toFile();
    }

    private File resolve(Id scope, Path path){
        //path isnt absolute ommited, because its private and its checked on call site (in Scope)
        return resolve(scope, path.toString());
    }

    private File resolve(ModificationEvent<Id> event){
        //type != deleted ommited, because its private and its checked on call site (in Scope)
        return resolve(event.getScope(), event.getPath());
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class Scope {
        @NonNull Id id;
        boolean isWriting;

        ModificationIndex<Id>.Scope modificationScope;

        public Scope(@NonNull Id id, boolean isWriting) {
            this.id = id;
            this.isWriting = isWriting;
            this.modificationScope  = modificationIndex.scopeFor(id, isWriting);
        }

        public File getScopeRoot(){
            return resolve(id);
        }

        public <T> T forReading(Path path, Function<File, T> body, Supplier<T> onMissing){
            //todo replace with dedicated exception
            require(!path.isAbsolute(), "Absolute paths cannot be resolved by file storage");
            return modificationScope
                .lastModificationOf(path)
                .filter(e -> e.getType() != Modification.DELETE)
                .map(FileIndex.this::resolve)
                .map(body)
                .orElseGet(onMissing);
        }

        public void forWriting(Path path, Consumer<File> body){
            require(isWriting, "Non-writing scope doesn't support forWriting(...) method");
            require(!path.isAbsolute(), "Absolute paths cannot be resolved by file storage");
            var resolved = resolve(id, path);
            resolved.getParentFile().mkdirs();
            body.accept(resolved);
            modificationScope.markWritten(path);
        }

        public void delete(Path path, Runnable onMissing){
            require(isWriting, "Non-writing scope doesn't support delete(...) method");
            require(!path.isAbsolute(), "Absolute paths cannot be resolved by file storage");
            if (uncheckedExists(path)){
                modificationScope.markDeleted(path);
            } else {
                onMissing.run();
            }
        }

        /**
         * Same as exists(...), but without any contract checks
         */
        private boolean uncheckedExists(Path path){
            var lastModification = modificationScope.lastModificationOf(path);
            return lastModification.isPresent() && lastModification.get().getType() != Modification.DELETE;
        }

        public boolean exists(Path path){
            require(!path.isAbsolute(), "Absolute paths cannot be resolved by file storage");
            return uncheckedExists(path);
        }

        public void purge(){
            modificationScope.purge();
        }

    }

    public Scope scopeFor(Id id, boolean isWriting){
        return new Scope(id, isWriting);
    }

}
