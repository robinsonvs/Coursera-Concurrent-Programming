package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import static edu.rice.pcdp.PCDP.finish;
/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit)
    {
        //throw new UnsupportedOperationException();

        final SieveActorActor actor = new SieveActorActor();
        finish(() -> {
            for (int n = 3; n <= limit; n += 2) {
                actor.send(n);
            }
            actor.send(0);
        });

        // Sum up the number of local primes from each actor in the chain.
        int totPrimes = 1;
        SieveActorActor current = actor;
        while (current != null) {
            totPrimes += current.numLocalPrimes;
            current = current.nextActor;
        }

        return totPrimes;

    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */

        //throw new UnsupportedOperationException();

        static final int MAX_LOCAL_PRIMES = 1000;

        SieveActorActor nextActor = null;

        int[] localPrimes = new int[MAX_LOCAL_PRIMES];
        int numLocalPrimes = 0;

        @Override
        public void process(final Object theMsg) {
            int candidate = (int) theMsg;

            // Special message indicating that we should terminate child actors and exit.
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(theMsg);
                }
                return;
            }

            // If it's not locally prime, ignore it.
            if (!isLocalPrime(candidate)) {
                return;
            }

            // If there is still room, remember the candidate and stop.
            if (numLocalPrimes < MAX_LOCAL_PRIMES) {
                localPrimes[numLocalPrimes++] = candidate;
                return;
            }

            // No room, send the candidate down the chain.
            if (nextActor == null) {
                nextActor = new SieveActorActor();
            }

            nextActor.send(theMsg);
        }


        boolean isLocalPrime(int candidate) {
            for (int n = 0; n < numLocalPrimes; n++) {
                // If the candidate divides on anything from local store,
                // it's not a prime.
                if (candidate % localPrimes[n] == 0) {
                    return false;
                }
            }

            return true;
        }
    }
}