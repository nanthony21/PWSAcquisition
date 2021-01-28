///////////////////////////////////////////////////////////////////////////////
//PROJECT:       PWS Plugin
//
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nick Anthony, 2021
//
// COPYRIGHT:    Northwestern University, 2021
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
package edu.bpl.pwsplugin.acquisitionSequencer;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author Nick Anthony <nickmanthony at hotmail.com>
 */
public interface ThrowingFunction<T, R> extends Function<T, R> {
    @Override
    default R apply(T t){
        try{
            return applyThrows(t);
        } catch (RuntimeException rte) { // Just let the runtime exception propagate
            throw rte;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ie);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t) throws Exception;
    
    default <V> ThrowingFunction<T, V> andThen(ThrowingFunction<? super R, ? extends V> after){
        Objects.requireNonNull(after);
        try{
             return (T t) -> after.apply(apply(t));
        } catch (RuntimeException rte) { // Just let the runtime exception propagate
            throw rte;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    default <V> ThrowingFunction<V, R> compose(ThrowingFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        try {
            return (V v) -> apply(before.apply(v));
        } catch (RuntimeException rte) { // Just let the runtime exception propagate
            throw rte;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}