/********************************************************************
 *                                                                  *
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.   *
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS     *
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE *
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.       *
 *                                                                  *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2002             *
 * by the Xiph.Org Foundation http://www.xiph.org/                  *
 *                                                                  *
 ********************************************************************/

package mods.thecomputerizer.musictriggers.libraries.org.xiph.libvorbis;

import mods.thecomputerizer.musictriggers.libraries.org.xiph.libvorbis.vorbis_constants.integer_constants;

class vorbis_look_floor1 {

	int[] sorted_index;	// int sorted_index[VIF_POSIT+2];
	int[] forward_index;	// int forward_index[VIF_POSIT+2];
	int[] reverse_index;	// int reverse_index[VIF_POSIT+2];

	int[] hineighbor;	// int hineighbor[VIF_POSIT];
	int[] loneighbor;	// int loneighbor[VIF_POSIT];
	int posts;

	int n;
	int quant_q;
	vorbis_info_floor1 vi;

	int phrasebits;
	int postbits;
	int frames;


	public vorbis_look_floor1() {

		sorted_index = new int[ integer_constants.VIF_POSIT+2 ];
		forward_index = new int[ integer_constants.VIF_POSIT+2 ];
		reverse_index = new int[ integer_constants.VIF_POSIT+2 ];

		hineighbor = new int[ integer_constants.VIF_POSIT ];
		loneighbor = new int[ integer_constants.VIF_POSIT ];
	}
}