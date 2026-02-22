# Documentation contribution and versioning

This file describes how to author documentation for the project and how the release publishing workflow works.

Overview
- The public docs site is published to the `gh-pages` branch under a versioned path for each release tag (for example: `/1.0/`).
- Publishing is triggered automatically by a GitHub Release (workflow runs on `release` event, type `created`).
- The workflow builds `target/site` using Maven (`javadoc:aggregate` + `site`) and publishes the built site into a versioned folder on `gh-pages`.

Authoring pages
- Add or edit Markdown files under `docs/`. The site build aggregates module apidocs plus site resources into `target/site`.
- Keep content focused and include headings. Use relative links for local navigation.

Front-matter / versioned pages
- The current site build uses raw Markdown without a static-site front-matter processor. If you need per-version content, add files within a version-specific docs path and document that in the contributing notes.

Previewing locally
1. From the repository root run:

```bash
mvn -DskipTests -B clean javadoc:aggregate site
```

2. Open `target/site/index.html` in a browser to preview the generated site (narrative pages and `apidocs/` must be present).

Triggering publication
- Create a GitHub Release (tag) via the UI or `git` — the workflow triggers on `release` with type `created`.
- The workflow will publish to `gh-pages` into a folder named after the release tag.

Versions and redirects
- The `gh-pages` root contains `versions.json` (machine-readable array of published tags), a human `index.html` listing versions, and a `latest/` redirect to the newest release.

Maintainer notes
- If `javadoc` generation fails on any module, the parent POM includes `failOnError=false` for the `maven-javadoc-plugin` to avoid blocking publication; consider fixing Javadoc warnings regularly.
- If your repository enforces branch protection on `gh-pages`, allow GitHub Actions to push or use an appropriate deploy token.
