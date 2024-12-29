# Table module changelog

The unit tests,

`uk/ac/starlink/table`
- `FormatsTest`
- `HealpixTest`
- `SchemaTest`

`uk/ac/starlink/table/storage`
- `StorageTest`

depend on `fits`, `votable` modules which also depend on `table` module.

They have been moved to `<project>/src/test/java/` temporary so that the `table` module can be built.