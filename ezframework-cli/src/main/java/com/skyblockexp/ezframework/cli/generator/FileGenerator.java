package com.skyblockexp.ezframework.cli.generator;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public final class FileGenerator {
    private FileGenerator() {}

    public static Path generateFromStub(String stubName, String pkg, String cls, Path base) throws IOException {
        String stub = StubLoader.load(stubName);
        Map<String, String> vars = new HashMap<>();
        String pkgLine = pkg.isEmpty() ? "" : "package " + pkg + ";";
        vars.put("PACKAGE_LINE", pkgLine);
        vars.put("PACKAGE", pkg);
        vars.put("CLASS", cls);
        vars.put("CLASS_LOWER", cls.toLowerCase());

        String content = TemplateProcessor.process(stub, vars);

        Path targetDir = base.resolve("src/main/java/" + pkg.replace('.', '/'));
        Files.createDirectories(targetDir);
        Path file = targetDir.resolve(cls + ".java");
        if (Files.exists(file)) throw new FileAlreadyExistsException(file.toString());
        Files.writeString(file, content, StandardOpenOption.CREATE_NEW);
        return file;
    }
}
