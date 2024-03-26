## PathEmpty:

A PathEmpty is a relative-ref

It can be set on a relative-ref with or without an authority.
It can be set on a URL.
It can be set on a URI.

## PathRootlessNoColon (`path-noscheme`):

A PathEmpty is a relative-ref

It cannot be set on a relative-ref if it has an authority. It would alter the authority in its string form
It can be set on a relative-ref without an authority
It cannot be set on a URL. It would alter the authority in its string form
It can be set on a URI.

`p1`
`p1/p2/p3`
`p1/`
`p1//`
`p1/p2/`
`p1//p3`


## PathRootlessWithColon (`path-rootless`):

A PathEmpty is NOT a relative-ref. The first segment would make it a URI as it would be the scheme.

It cannot be set on a relative-ref if it has an authority. That would alter the authority in its string form
It cannot be set on a relative-ref without an authority. The first segment would make it a URI as it would be the scheme.
It cannot be set on a URL. It would alter the authority in its string form
It can be set on a URI.

`p:1`
`p:1/p2/p3`
`p:1/`
`p:1//`
`p:1/p2/`
`p:1//p3`

## PathAbsolute (`path-absolute`):

A PathAbsolute is a relative-ref.

It can be set on a relative-ref with or without an authority.
It can be set on a URL.
It can be set on a URI.

`/`     - `["", ""]`
`/p2`   - `["", "p2"]`
`/p2/`  - `["", "p2", ""]`
`/p2/p3`- `["", "p2", "p3"]`

## AmbiguousPathAbsolute (`path-abempty`):

An AmbiguousPathAbsolute is NOT a relative-ref. The third segment would be the authority.
It can be set on a relative-ref if it has an authority
It cannot be set on a relative-ref without an authority. That would turn its string form into a reference with an authority
It can be set on a URL.
It cannot be set on a URI. That would turn its string form into a URL.

`//`    - `["", "", ""]`
`//p3`  - `["", "", "p3"]`

|               | relative-ref? | Set relative-ref with authority | Set relative-ref without authority | Set URL | Set URI |
|---------------|---------------|---------------------------------|------------------------------------|---------|---------|
| path-empty    | Y             | Y                               | Y                                  | Y       | Y       |
| path-absolute | Y             | Y                               | Y                                  | Y       | Y       |
| path-noscheme | Y             | N                               | Y                                  | N       | Y       |
| path-rootless | N             | N                               | N                                  | N       | Y       |
| path-abempty  | N             | Y                               | N                                  | Y       | N       |
