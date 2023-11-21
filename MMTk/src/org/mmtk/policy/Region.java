package org.mmtk.policy.region;

import org.mmtk.vm.VM;
import org.mmtk.utility.Constants;
import org.vmmagic.pragma.Inline;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.*;

@Uninterruptible
public class Region {
  public static final int EDEN = 0;
  public static final int SURVIVOR = 1;
  public static final int OLD = 2;

  public static final int LOG_PAGES_IN_REGION = 8;
  public static final int PAGES_IN_REGION = 1 << LOG_PAGES_IN_REGION;
  public static final int LOG_BYTES_IN_REGION = LOG_PAGES_IN_REGION + LOG_BYTES_IN_PAGE;
  public static final int BYTES_IN_REGION = 1 << LOG_BYTES_IN_REGION;
  public static final Word REGION_MASK = Word.fromIntZeroExtend(BYTES_IN_REGION - 1);
  public static int PREV = 0;
  public static int CURSOR = PREV;
  
  @Inline
  private static Address align(final Address ptr) {
    return ptr.toWord().and(REGION_MASK.not()).toAddress();
  }

  @Inline
  public static boolean isAligned(Address region) {
    return region.toWord().and(REGION_MASK).isZero();
  }

  @Inline
  public static Address allocate(final Address region, final int size) {
    if (VM.VERIFY_ASSERTIONS) VM.assertions._assert(isAligned(region));
    Address regionEnd = region.plus(BYTES_IN_REGION);
    // get the current address of the cursor
    Address currAddress = getAddressOf(region, CURSOR);
    Address addressTill = currAddress.plus(size);
    if (AddressTill.GT(regionEnd)) return Address.zero(); // if address goes out of bounds of the determined scope
    CURSOR += size;
    return currAddress;
  }
  
  @Inline
  public static Address getAddressOf(Address region, int offset) {
	  // logic to get the address of the cursor
	  return Address.zero();
  }
  
  static String getGenerationName(Address region) {
	  int gen = getAddressOf(region, CURSOR).loadInt(); // but this only gets generation of the current region where the cursor is at. But we might/ will need this at several instances where the objects must be moved from one space to other. So there is a necessity to maintain metadata as well. 
	  switch (gen) {
	      case EDEN:     return "eden";
	      case SURVIVOR: return "survivor";
	      case OLD:      return "old";
	      default:       return "???";
	    }
	}

  // Additional methods needed for allocation and movement of objects
}
