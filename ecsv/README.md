# Note on unit test

Certain asserts are commented out as the methods' signatures are not available in junit 5.

For example, in `ecsv/src/test/java/uk/ac/starlink/ecsv/EcsvTest.java`

```java
@Test
public void testSubtypes() throws IOException {
    
    // no comparison between long[] and Object
    assertArrayEquals( new long[] { 8, 9, 10 }, s2.getCell( 2, 0 ) );
    
}
```

The module `uk.ac.starlink.util.TestCase` in `test/` does provide the assert methods, but the class is in the test scope.
