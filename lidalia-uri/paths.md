## PathEmpty:

A PathEmpty is a relative-ref

It can be set on a relative-ref with or without an authority.
It can be set on a URL.
It can be set on a URI.

## PathRootlessNoColon (`path-noscheme`):

A PathEmpty is a relative-ref

It cannot be set on a relative-ref if it has an authority. It would alter the authority in its
string form
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

It cannot be set on a relative-ref if it has an authority. That would alter the authority in its
string form
It cannot be set on a relative-ref without an authority. The first segment would make it a URI as it
would be the scheme.
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
It cannot be set on a relative-ref without an authority. That would turn its string form into a
reference with an authority
It can be set on a URL.
It cannot be set on a URI. That would turn its string form into a URL.

`//`    - `["", "", ""]`
`//p3`  - `["", "", "p3"]`

|               | examples                | relative-ref? | ambiguous? | absolute? |
|---------------|-------------------------|---------------|------------|:----------|
| path-empty    | ``                      | Y             | N          | N         |
| path-abempty  | `//`, `//p2`            | N             | Y          | Y         |
| path-absolute | `/`, `/p2`              | Y             | N          | Y         |
| path-rootless | `p:1`, `p:1/`, `p:1/p2` | N             | Y          | N         |
| path-noscheme | `p1`, `p1/`, `p1/p2`    | Y             | N          | N         |

Ambiguous absolute      YY  path-abempty
Unambiguous absolute    NY  path-absolute
Ambiguous relative      YN  path-rootless
Unambiguous relative    NN  path-empty, path-noscheme
