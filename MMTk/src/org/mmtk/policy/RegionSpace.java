package org.mmtk.policy.region;

import org.mmtk.plan.TraceLocal;
import org.mmtk.plan.TransitiveClosure;
import org.mmtk.policy.Space;
import org.mmtk.utility.ForwardingWord;
import org.mmtk.utility.heap.*;
import org.mmtk.vm.VM;
import org.vmmagic.pragma.*;
import org.vmmagic.unboxed.*;

/**
 * Simplified RegionSpace class for single-threaded environment.
 */
@Uninterruptible
public final class RegionSpace extends Space {
	
	private boolean fromSpace = true;

  @Entrypoint private Address headRegion = Address.zero();
  
  int nurseryRegions = 0;
  public int regionsInUse = 0;

  public RegionSpace(String name) {
    super(name, true, false, true, VMRequest.discontiguous());
    pr = new FreeListPageResource(this, Region.METADATA_PAGES_PER_CHUNK);
  }

  public Address acquireRegion(int generation) {
	    // Acquire pages for the new region
	    int pagesReserved = pr.reservePages(Region.PAGES_IN_REGION);
	    Address region = pr.getNewPages(pagesReserved, Region.PAGES_IN_REGION, zeroed);
	    if (region.isZero()) {
	        return Address.zero(); // Failed to allocate new region
	    }

	    // Initialize the new region
	    if (generation != Region.OLD) {
	        nurseryRegions += 1;
	    }
	    regionsInUse += 1;

	    Region.register(region, generation);
	    Region.set(region, Region.CURSOR, headRegion);
	    headRegion = region;

	    return region;
	}


  @Inline
  public void releaseRegion(Address region) {
      // Clear metadata -- write unregister method in Region
      Region.unregister(region);

      // Release memory
      ((FreeListPageResource) pr).releasePages(region);
  }
  
  @Override
  public boolean isLive(ObjectReference object) {
    return ForwardingWord.isForwarded(object);
  }

  @Override
  public boolean isReachable(ObjectReference object) {
    return !fromSpace || ForwardingWord.isForwarded(object);
  }

  // Other necessary methods and fields to be included to trace and sweep
}
