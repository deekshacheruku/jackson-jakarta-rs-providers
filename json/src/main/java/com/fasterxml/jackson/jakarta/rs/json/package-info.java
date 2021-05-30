/**
 * Jackson-based Jakarta-RS provider that can automatically
 * serialize and deserialize resources for 
 * JSON content type (MediaType).
 *<p>
 * Also continues supporting functionality, such as
 * exception mappers that can simplify handling of
 * error conditions.
 *<p>
 * There are two default provider classes:
 *<ul>
 * <li>{@link com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider} is the basic
 *    provider configured to use Jackson annotations
 *  </li>
 * <li>{@link com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider} is extension
 *    of the basic provider, configured to additionally use Jakarta XmlBind annotations,
 *    in addition to (or in addition of, if so configured) Jackson annotations.
 *  </li>
 * </ul>
 */
package com.fasterxml.jackson.jakarta.rs.json;
