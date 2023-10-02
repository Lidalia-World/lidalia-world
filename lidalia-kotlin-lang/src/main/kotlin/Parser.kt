package uk.org.lidalia.lang

import arrow.core.Either

interface CharSequenceParser<E : Exception, out O> : (CharSequence) -> Either<E, O>
