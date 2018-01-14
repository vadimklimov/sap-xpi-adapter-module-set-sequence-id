# SAP PI/PO: Adapter module SetSequenceIdBean
Custom adapter module for Adapter Engine of SAP PI/PO systems to set dynamic sequence ID of the processed message.

Usage of the adapter module is described in SAP Community blog https://blogs.sap.com/2016/08/15/setting-dynamic-queue-name-in-eoio-scenarios/.


## Adapter module parameterization
|Adapter module parameter|Description|
|---|---|
|xpath|XPath expression that shall extract required payload element’s value for dynamic sequence ID.|
|error.terminate|Terminate execution of the module if sequence ID cannot be set, causing also termination of further message processing by a channel. Not recommended to be disabled if dynamic sequence ID generation is a must.|
|sequenceId.deleteLeadingCharacter|Delete leading characters from the extracted value for the specified XPath expression.|
|sequenceId.leadingCharacter|Leading character.|
|sequenceId.replaceInvalidCharacters|Replace all invalid (not alphanumeric) characters from the constructed sequence ID with underscore symbol ('_').|
|sequenceId.truncate|Truncate constructed sequence ID so that its length does not exceed restrictions applicable for sequence ID (which is, 16 characters) – truncation can be either done to trim beginning of the value or its end. If the value retrieved using the specified XPath, is object identifier (e.g. customer number, sales order number, employee ID) that is iteratively increased for every subsequent instance of an object (e.g. sender system uses number range object for it), then truncation from start may be preferable, leaving a value part varying between closely following instances of objects.|
|sequenceId.prefix|Fixed prefix value for a constructed sequence ID, which will be delimited from remaining sequence ID value part with uderscore symbol ('_'). Shall not normally contain many characters since it may negatively impact overall length of the constructed sequence ID – optimum is 2-3 characters. From perspective of impact on total sequence ID value length, it is not recommended to use prefix and suffix at the same time. Note that if truncation is enabled for start of the value, prefix may become truncated.|
|sequenceId.suffix|Fixed suffix value for a constructed sequence ID, which will be delimited from remaining sequence ID value part with uderscore symbol ('_'). Shall not normally contain many characters since it may negatively impact overall length of the constructed sequence ID – optimum is 2-3 characters. From perspective of impact on total sequence ID value length, it is not recommended to use prefix and suffix at the same time. Note that if truncation is enabled for end of the value, suffix may become truncated.|
