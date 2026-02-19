package com.skyblockexp.ezframework.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Command(name = "ez", mixinStandardHelpOptions = true, description = "EzFramework helper CLI", subcommands = {Ez.MakeRepo.class, Ez.MakeCmd.class, Ez.MakeMigration.class})
public class Ez implements Callable<Integer> {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Ez()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("EzFramework CLI - use subcommands 'make:repo' and 'make:cmd'");
        return 0;
    }

    @Command(name = "make:repo", description = "Scaffold a Repository class. Provide a fully-qualified class name.")
    static class MakeRepo implements Callable<Integer> {
        @Parameters(index = "0", description = "Fully-qualified class name, e.g. com.example.plugin.repo.PlayerRepository")
        private String fqcn;

        @Option(names = {"-m", "--module"}, description = "Target module directory (defaults to project root)")
        private String module = ".";

        @Override
        public Integer call() throws Exception {
            String pkg = packageOf(fqcn);
            String cls = classOf(fqcn);
            Path base = resolveModulePath(module);
            try {
                java.nio.file.Path created = com.skyblockexp.ezframework.cli.generator.FileGenerator.generateFromStub("repo.stub", pkg, cls, base);
                System.out.println("Created: " + created);
                return 0;
            } catch (java.nio.file.FileAlreadyExistsException faee) {
                System.err.println("File already exists: " + faee.getMessage());
                return 1;
            } catch (IOException ioe) {
                System.err.println("Failed to generate file: " + ioe.getMessage());
                return 2;
            }
        }
    }

    @Command(name = "make:cmd", description = "Scaffold an EzCmd class. Provide a fully-qualified class name.")
    static class MakeCmd implements Callable<Integer> {
        @Parameters(index = "0", description = "Fully-qualified class name, e.g. com.example.plugin.cmd.ExampleCmd")
        private String fqcn;

        @Option(names = {"-m", "--module"}, description = "Target module directory (defaults to project root)")
        private String module = ".";

        @Override
        public Integer call() throws Exception {
            String pkg = packageOf(fqcn);
            String cls = classOf(fqcn);
            Path base = resolveModulePath(module);
            try {
                java.nio.file.Path created = com.skyblockexp.ezframework.cli.generator.FileGenerator.generateFromStub("cmd.stub", pkg, cls, base);
                System.out.println("Created: " + created);
                return 0;
            } catch (java.nio.file.FileAlreadyExistsException faee) {
                System.err.println("File already exists: " + faee.getMessage());
                return 1;
            } catch (IOException ioe) {
                System.err.println("Failed to generate file: " + ioe.getMessage());
                return 2;
            }
        }
    }

    @Command(name = "make:subcmd", description = "Scaffold a Subcommand class. Provide a fully-qualified class name.")
    static class MakeSubcmd implements Callable<Integer> {
        @Parameters(index = "0", description = "Fully-qualified class name, e.g. com.example.plugin.cmd.ExampleSub")
        private String fqcn;

        @Option(names = {"-m", "--module"}, description = "Target module directory (defaults to project root)")
        private String module = ".";

        @Override
        public Integer call() throws Exception {
            String pkg = packageOf(fqcn);
            String cls = classOf(fqcn);
            Path base = resolveModulePath(module);
            try {
                java.nio.file.Path created = com.skyblockexp.ezframework.cli.generator.FileGenerator.generateFromStub("subcmd.stub", pkg, cls, base);
                System.out.println("Created: " + created);
                return 0;
            } catch (java.nio.file.FileAlreadyExistsException faee) {
                System.err.println("File already exists: " + faee.getMessage());
                return 1;
            } catch (IOException ioe) {
                System.err.println("Failed to generate file: " + ioe.getMessage());
                return 2;
            }
        }
    }

    @Command(name = "make:migration", description = "Scaffold a SQL migration file.")
    static class MakeMigration implements Callable<Integer> {
        @Parameters(index = "0", description = "Migration filename, e.g. 001_create_users_table.sql")
        private String filename;

        @Parameters(index = "1", arity = "0..1", description = "Description (optional)")
        private String description = "";

        @Option(names = {"-m", "--module"}, description = "Target module directory (defaults to project root)")
        private String module = ".";

        @Override
        public Integer call() throws Exception {
            Path base = resolveModulePath(module);
            try {
                String stub = com.skyblockexp.ezframework.cli.generator.StubLoader.load("migration.stub");
                Map<String,String> vars = new HashMap<>();
                vars.put("NAME", filename);
                vars.put("DESCRIPTION", description == null ? "" : description);
                String content = com.skyblockexp.ezframework.cli.generator.TemplateProcessor.process(stub, vars);

                Path targetDir = base.resolve("src/main/resources/migrations");
                Files.createDirectories(targetDir);
                Path file = targetDir.resolve(filename);
                if (Files.exists(file)) throw new java.nio.file.FileAlreadyExistsException(file.toString());
                Files.writeString(file, content, StandardOpenOption.CREATE_NEW);
                System.out.println("Created: " + file);
                return 0;
            } catch (java.nio.file.FileAlreadyExistsException faee) {
                System.err.println("File already exists: " + faee.getMessage());
                return 1;
            } catch (IOException ioe) {
                System.err.println("Failed to generate file: " + ioe.getMessage());
                return 2;
            }
        }
    }

    @Command(name = "make:migration-java", description = "Scaffold a Java Migration class.")
    static class MakeMigrationJava implements Callable<Integer> {
        @Parameters(index = "0", description = "Fully-qualified class name, e.g. com.example.plugin.migrations.AddUsersTable")
        private String fqcn;

        @Parameters(index = "1", arity = "0..1", description = "Migration id (optional)")
        private String id = "";

        @Option(names = {"-m", "--module"}, description = "Target module directory (defaults to project root)")
        private String module = ".";

        @Override
        public Integer call() throws Exception {
            String pkg = packageOf(fqcn);
            String cls = classOf(fqcn);
            Path base = resolveModulePath(module);
            try {
                java.nio.file.Path created = com.skyblockexp.ezframework.cli.generator.FileGenerator.generateFromStub("migration-java.stub", pkg, cls, base);
                // Optionally write META-INF/services entry helper
                System.out.println("Created: " + created);
                return 0;
            } catch (java.nio.file.FileAlreadyExistsException faee) {
                System.err.println("File already exists: " + faee.getMessage());
                return 1;
            } catch (IOException ioe) {
                System.err.println("Failed to generate file: " + ioe.getMessage());
                return 2;
            }
        }
    }

    private static Path resolveModulePath(String module) {
        Path p = Path.of(module);
        if (Files.exists(p) && Files.isDirectory(p)) return p;
        // try project submodule
        Path sub = Path.of(module);
        return Path.of(".");
    }

    private static String packageOf(String fqcn) {
        int i = fqcn.lastIndexOf('.');
        return (i == -1) ? "" : fqcn.substring(0, i);
    }

    private static String classOf(String fqcn) {
        int i = fqcn.lastIndexOf('.');
        return (i == -1) ? fqcn : fqcn.substring(i + 1);
    }

    // Templates and stub loading are now handled by generator classes under
    // com.skyblockexp.ezframework.cli.generator. This ensures stubs are the
    // single source of truth and removes inline class strings.
}
