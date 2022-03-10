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

class vorbis_block_internal {

	float[][] pcmdelay;	// **pcmdelay // this is a pointer into local storage
  	float ampmax;
  	int blocktype;

  	oggpack_buffer[] packetblob;


	public vorbis_block_internal() {

		packetblob = new oggpack_buffer[ integer_constants.PACKETBLOBS ];
	}
}