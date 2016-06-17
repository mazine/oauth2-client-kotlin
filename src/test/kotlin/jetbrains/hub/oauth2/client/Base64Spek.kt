package jetbrains.hub.oauth2.client

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class Base64Spek : Spek({
    describe("Base64") {
        on("encode") {
            given("empty array") {
                it("should return empty") {
                    assertEquals("", Base64.encode(byteArrayOf()))
                }
            }

            given("some byte array") {
                val string = "1234-3213-3123:topsecret".toByteArray()
                it("should return it base64 encoded") {
                    assertEquals("MTIzNC0zMjEzLTMxMjM6dG9wc2VjcmV0", Base64.encode(string))
                }
            }

            given("some byte array of length mod 3 equal 1") {
                val string = "1234-3213-3123:topsecr".toByteArray()
                it("should pad it with ==") {
                    assertEquals("MTIzNC0zMjEzLTMxMjM6dG9wc2Vjcg==", Base64.encode(string))
                }
            }

            given("some byte array of length mod 3 equal 2") {
                val string = "1234-3213-3123:topsecre".toByteArray()
                it("should pad it with =") {
                    assertEquals("MTIzNC0zMjEzLTMxMjM6dG9wc2VjcmU=", Base64.encode(string))
                }
            }
        }
    }
})