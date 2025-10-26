## Development Workflow

This project follows a branching strategy to maintain a stable `master` branch for releases.

*   **`master` branch:** Contains the latest stable release. This branch should always be in a deployable state.
*   **`develop` branch:** The primary branch for integrating new features and bug fixes. All development work is merged here first.
*   **`feature`/`bugfix` branches:** New work (features, bug fixes, refactors) should be done in dedicated branches created from the `develop` branch (e.g., `feature/add-xyz`, `bugfix/issue-123`).
    *   Create your feature branch: `git checkout -b feature/your-feature develop`
    *   Make your changes, commit them.
    *   Push your branch: `git push origin feature/your-feature`
    *   Open a Pull Request (PR) from your feature branch to the `develop` branch.
*   **Releases:**
    *   When `develop` is ready for a new release, a PR is opened to merge `develop` into `master`.
    *   This PR is reviewed and merged.
    *   After merging into `master`, a new Git tag (e.g., `v1.0.0`) is created on the `master` branch to mark the release.
