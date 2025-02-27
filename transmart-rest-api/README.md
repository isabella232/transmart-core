# tranSMART RESTful API documentation

### OpenAPI specification

Since `transmart-rest-api` has been merged into a single _tranSMART_ repository, the REST API uses versioning by using
version prefixes on every call. The previously existing calls are now prefixed with `/v1`, to make room for a new `/v2` API.
The API versions have been documented using the Open API Specification, based on [Swagger](https://swagger.io/).
See the [open-api](../open-api) specification for further documentation on the API versions of _tranSMART_.

This page still contains the documentation of the `v1` API.


### Available calls for the tranSMART RESTful API

The following is a list of all HTTP requests that can be made to a tranSMART RESTful API. All URI's are relative to your tranSMART server's URI (e.g., `https://some.transmart.server/`).

The return message body will contain JSON (or HAL+JSON) format, with the exception of the request for high dimensional data (see section below).

| Call | HTTP request | Description |
| --- | --- | --- |
| get all studies | GET `/v1/studies`  | returns information on all available studies |
| get single study | GET `/v1/studies/{studyId}`  | returns information on a single study |
| get all concepts | GET `/v1/studies/{studyId}/concepts` | returns information on all concepts for a study |
| get single concept | GET `/v1/studies/{studyId}/concepts/{conceptPath}` | returns information on one concept for a study |
| get all subjects | GET `/v1/studies/{studyId}/subjects` | returns information on all subjects for a study |
| get single subject | GET `/v1/studies/{studyId}/subjects/{subjectId}` | returns information on one subject for a study |
| get subjects with concept | GET `/v1/studies/{studyId}/concepts/{conceptPath}/subjects` | returns the subjects which have data for this concept |
| get all observations | GET `/v1/studies/{studyId}/observations` | returns all clinical observation values for a study |
| get observations subj to certain criteria | GET `/v1/observations` | returns all clinical observation according to some filters. These are `patient_sets` (list of result instance ids or single patient set id (integer); cannot be combined with `patients`), `patients` (list of patient ids or single patient id (integer); cannot be combined with `patient_sets`; avoid specifying a large list for performance reasons), `concept_paths` (list of concepts paths or single concept path (string); mandatory). Multiple values are specified in the usual fashion for url parameters. There is one extra parameter: `variable_type` -- this specifies the type of `ClinicalVariable` to create (defaults to `normalized_leafs_variable`) |
| get observations for single concept | GET `/v1/studies/{studyId}/concepts/{conceptPath}/observations` | returns clinical observation values for one concept for a study |
| create patient set | POST `/v1/patient_sets` | body should be query definition in a subset of i2b2's XML schema. Response is the same as for GET `/patient_sets/{result_instance_id}`  |
| get single patient set | GET `/v1/patient_sets/{resultInstanceId}` | returns information a previously created patient set |
| get index of highdim data for single concept| GET `/v1/studies/{studyId}/concepts/{conceptPath}/highdim` | returns index with the available datatype and projections, assay constraints and data constraints for this highdim concept for a study |
| get highdim data for single concept| GET `/v1/studies/{studyId}/concepts/{conceptPath}/highdim?dataType={dataType}&projection={projectionType}&assayConstraints={assayConstraints}&dataConstraints={dataConstraints}` | returns highdim data of a specific dataType and projection for one concept of a study |


#### Explanation of URI variables
| variable  | explanation |
| --- | --- |
| {studyId} | The id of the study, as returned by the `/studies` call. |
| {conceptPath} | A path that defines the concept within a study. This is similar to the concept path as defined within tranSMART, but without the initial part that defines the study path (and with necessary character conversion to make it compatible with URI syntax). The safest and most robust method of obtaining this value is by making use of the embedded links in the `/studies/{studyId}/concepts` result. |
| {subjectId} | A unique subject identifier, as returned by `/studies/{studyId}/subjects` call. |
| {dataType} | High dimensional concepts can be of several types, depending on what your tranSMART version supports. Possible data type options are contained in the highdim index returned by the `/studies/{studyId}/concepts/{conceptPath}/highdim` call. |
| {projectionType} | High dimensional data can have values stored in a variety of projections. Possible projection options are contained in the highdim index returned by the `/studies/{studyId}/concepts/{conceptPath}/highdim` call. If not specified, it defaults to `'default_real_projection'` or, if not supported `'all_data'`. It is *strongly* recommended that the user include a value for this parameter and do not rely on the defaults. |
| {assayConstraints} | Assay constraints limit the assays included in the response. This is a urlencoded JSON object. The keys are the names of the constraints and each value is either 1) an object where the keys are the corresponding constraint's parameters and the values are the parameters' values or 2) an array of such objects. The parameters for the constraints are not described in the API. Look at core-api's documentation; for the standard constraints, see the [`AssayConstraint`](https://github.com/transmart/transmart-core-api/blob/master/src/main/groovy/org/transmartproject/core/dataquery/highdim/assayconstraints/AssayConstraint.groovy) class. |
| {dataConstraints} | Analogous to {assayConstraints}, except it limits the rows in the high dimensional result. For the standard data constraints, see [`DataConstraint`](https://github.com/transmart/transmart-core-api/blob/master/src/main/groovy/org/transmartproject/core/dataquery/highdim/dataconstraints/DataConstraint.groovy) |

#### HTTP exchange details
Each of the above GET requests needs two header fields set:

1. Authentication: if your tranSMART server requires OAuth authentication (see section on OAuth below), then this header needs to have a value set to `Bearer {accessToken}`.
2. Accept: the return message will contain a body that contains JSON with HAL format if this field is set to `application/hal+json`; Otherwise, plain JSON will be returned (`application/json`).

Here is an example HTTP request for the `/v1/studies` call:

    GET /v1/studies HTTP/1.1
    Host: some.transmart.server
    Authorization: Bearer 12345-abcde
    Accept: application/hal+json
    
    
### High dimensional protobuf data
To facilitate the many variants of high dimensional data formats to be returned by tranSMART, we have chosen to use Google's protobuf solution. Whereas all other calls return a body containing JSON with additional HAL format, the request for high dimensional data returns a binary protobuf stream.

This binary stream can be parsed using the protobuf library, the implementation of which depends on the libraries available for your client application. The exact structure of the incoming data is defined in the [highdim.proto](src/protobuf/highdim/highdim.proto) file. This proto file must be included in your client application's resources and loaded by your protobuf instance, for it allows the correct parsing of the messages contained in the binary stream.

There are two types of messages contained in the binary stream, which need to be parsed differently. The first message contains header information, and all subsequent messages will each contain information on one row of your dataset.

The header message primarily contains the definition of all assays in your dataset, for which the subsequent row messages will contain the values. An assay is usually equivalent to a single subject, but please see the `highdim.proto` file for what else is definable per assay (under `message Assay`). In addition to the assay information, the header message also defines the name and type for each of the returned values in the row messages (under `message ColumnSpec`)

Each row message contains one `ColumnValue` item per assay, and the order is identical to the order of the assay definitions in the header. The content of this item is defined in the `highdim.proto` file under `message ColumnValue`, and it can contain multiple values (eg. z-score and log-value), for which the name and type is defined in the header (under `message ColumnSpec`).

**NOTE:** If there is no measurement taken for a combination of assay and probe then `NaN` would be used as the value. This workaround was made later to avoid changing the protobuf declaration, so we could still use compressed arrays of primitive data types, but at the same time mark absence of a value.

Example code for how to parse the protobuf binary stream into something sensible, please see the [transmartRClient](https://github.com/transmart/RInterface/blob/master/R/getHighdimData.R).


### Authentication with OAuth

This plugin does not implement any authentication scheme.
However, it is usually deployed as part of [transmart-api-server](../transmart-api-server) which is configured to secures this plugin's resources
with the Spring Security and uses Keycloak for user management and authentication.

### Changelog

#### Versioned API (25 Jan 2017)

Since `transmart-rest-api` has been merged into a single _tranSMART_ repository, the REST API uses versioning by using
version prefixes on every call. The previously existing calls are now prefixed with `/v1`, to reserve namespace for a new `/v2` API.
See the [open-api](../open-api) specification for further documentation on the API versions.

#### Structure of the result of `/v1/studies` (10 Sept 2015)

As of commit [00df0c0](https://github.com/transmart/transmart-rest-api/commit/00df0c06c6ef89a6fcf9055d41401ceb99d3ec98), the structure of the JSON output of `/studies` has changed
from a plain list of studies `[ ... ]` to the object `{ "studies": [ ... ] }`.
This makes the result of `/studies` consistent with the result for other resource lists.
