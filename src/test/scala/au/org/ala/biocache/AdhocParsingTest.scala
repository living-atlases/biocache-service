package au.org.ala.biocache
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import au.org.ala.util.AdHocParser

@RunWith(classOf[JUnitRunner])
class AdhocParsingTest extends ConfigFunSuite {


  test("Test with verbatimlatlong"){
    expect(2) {
      AdHocParser.guessColumnHeaders(Array("dsads", "sdas")).size
    }
  }

  test("Test with verbatimlatlong 2"){
    expect(2) {
      AdHocParser.guessColumnHeaders(Array("-37º 3' 48'' S", "149º 54' 14'' E")).size
    }
  }

  test("2 verbatim coordinates"){
    val headers = AdHocParser.guessColumnHeaders(Array("-37º 3' 48'' S", "149º 54' 14'' E"))
    expect(2) {headers.length}
    expect("verbatimLatitude") { headers(0) }
    expect("verbatimLongitude") { headers(1) }
  }

//  test("taxon rank parsing"){
//    val headers = AdHocParser.guessColumnHeaders(Array("WGS84","species"))
//    expect("geodeticDatum") {headers(0)}
//    expect("taxonRank") {headers(1)}
//  }
}