# Commands — EzFramework

This folder documents command-related APIs and recommended patterns.

Recommended workflow:

- Production: create a class that `extends EzCmd` and register `Subcommand` implementations.
- Examples/tests: use `CommandBuilder` (convenience helper).

Files:

- [ez_cmd.md](ez_cmd.md) — core `EzCmd` reference and examples.
- [subcommand.md](subcommand.md) — patterns for `Subcommand` implementations.
- [command_builder.md](command_builder.md) — the convenience builder API (not the primary approach).
