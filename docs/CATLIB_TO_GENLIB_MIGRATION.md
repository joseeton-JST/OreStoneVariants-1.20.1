# CatLib -> GenLib Migration Guide

This repository currently uses a phased migration:

1. Phase 1 (OSV rebrand): OSV package/metadata move to `io.github.joseetoon.osv`.
2. Phase 2 (bridge): library branding is **GenLib**, but technical identity remains `catlib` for compatibility.
3. Phase 3 (final): technical cutover to `genlib`.

## Current bridge compatibility

- Mod id: `catlib`
- Java package: `personthecat.catlib`
- Existing consumers continue to work without code changes.

## Phase 3 target

- Mod id: `genlib`
- Java package root: `io.github.joseetoon.genlib`
- Maven coordinates:
  - `io.github.joseetoon:genlib-forge`
  - equivalent common/platform artifacts

## Consumer migration checklist (Phase 3)

1. Replace dependency coordinates from `personthecat:catlib-*` to `io.github.joseetoon:genlib-*`.
2. Replace imports:
   - `personthecat.catlib.*` -> `io.github.joseetoon.genlib.*`
3. Update `mods.toml` dependency from `catlib` to `genlib`.
4. Rebuild and validate startup, commands, config loading, and registry hooks.

