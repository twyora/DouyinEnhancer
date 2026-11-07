## 1. Module Entry Points

This module injects no custom UI into the host app besides its required settings page. It alters
host behavior by modifying underlying data streams. Use the host’s native features directly. If the
module loads correctly, you should see the expected changes in most cases.

## 2. Features Not Taking Effect

Confirm the module is activated properly. You can view module logs from your Xposed manager. Errors
indicating missing methods can usually be resolved by providing the obfuscation mapping file. For
other crashes, or cases where there are no visible errors but the feature does not work, you may
send a report to the author.

> Note: Compatibility with rootless hook frameworks is not currently targeted. The author cannot
> guarantee functionality in such environments.
> If you would like this scenario to be investigated, please include module logs with your report.
