package mods.thecomputerizer.musictriggers.api.core;

import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.MultiVersionCoreMod;
import mods.thecomputerizer.theimpossiblelibrary.api.core.asm.TypeHelper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.musictriggers.api.MTRef.VERSION;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.asm.ASMRef.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;

@SuppressWarnings("LoggingSimilarMessage")
@MultiVersionCoreMod(modid = MODID, modName = NAME, modVersion = VERSION)
public class MTCoreEntryPoint extends CoreEntryPoint {
    
    static final String EMPTY_DESC = EMPTY_METHOD.getDescriptor();
    static final String HANDLER_BINARY = getHandlerBinary();
    static final String HANDLER_NAME = Objects.nonNull(HANDLER_BINARY) ?
            HANDLER_BINARY.replace('.','/') : null;
    static final String HELPER_NAME = "mods/thecomputerizer/musictriggers/api/data/channel/ChannelHelper";
    static final String TICKER_BINARY = getTickerBinary();
    static final String TICKER_NAME = Objects.nonNull(TICKER_BINARY) ?
            TICKER_BINARY.replace('.','/') : null;
    static final String TICKER_DESC = TypeHelper.methodDesc(BOOLEAN_TYPE);
    
    //Mods that have their own music tickers need to be fixed as well
    static final String TICKER_GC_BINARY = getTickerGCBinary();
    static final String TICKER_GC_NAME = Objects.nonNull(TICKER_GC_BINARY) ?
            TICKER_GC_BINARY.replace('.','/') : null;
    static final String TICKER_SA_BINARY = getTickerSABinary();
    static final String TICKER_SA_NAME = Objects.nonNull(TICKER_SA_BINARY) ?
            TICKER_SA_BINARY.replace('.','/') : null;
    
    static String getHandlerBinary() {
        CoreAPI core = CoreAPI.getInstance();
        if(!core.isClientSide()) return null;
        boolean fabric = core.getModLoader().isFabric();
        if(core.getVersion().isV12()) return "net.minecraft.client.audio.SoundHandler";
        if(core.getVersion().isV16()) return fabric ? "net.minecraft.class_1144" : "net.minecraft.client.audio.SoundHandler";
        return fabric ? "net.minecraft.class_1144" : "net.minecraft.client.sounds.SoundManager";
    }
    
    static String getTickerBinary() {
        CoreAPI core = CoreAPI.getInstance();
        if(!core.isClientSide()) return null;
        boolean fabric = core.getModLoader().isFabric();
        if(core.getVersion().isV12()) return "net.minecraft.client.audio.MusicTicker";
        if(core.getVersion().isV16()) return fabric ? "net.minecraft.class_1142" : "net.minecraft.client.audio.MusicTicker";
        return fabric ? "net.minecraft.class_1142" : "net.minecraft.client.sounds.MusicManager";
    }
    
    static String getTickerGCBinary() {
        CoreAPI core = CoreAPI.getInstance();
        if(!core.isClientSide() || !core.getVersion().isV12()) return null;
        return "micdoodle8.mods.galacticraft.core.client.sounds.MusicTickerGC";
    }
    
    static String getTickerSABinary() {
        CoreAPI core = CoreAPI.getInstance();
        if(!core.isClientSide() || !core.getVersion().isV12()) return null;
        return "spaceambient.core.sounds.Ambient_MusicTicker";
    }
    
    final CoreAPI core;
    List<String> targets;
    
    public MTCoreEntryPoint() {
        this.core = CoreAPI.getInstance();
        TILRef.logInfo("Constructing MTCoreEntryPoint on ClassLoader {}", getClass().getClassLoader());
        if(this.core.isClientSide()) {
            TILRef.logInfo("SoundHandler name is {}",HANDLER_NAME);
            TILRef.logInfo("MusicTicker name is {}",TICKER_NAME);
        }
    }
    
    @Override public List<String> classTargets() {
        if(Objects.isNull(this.targets))
            this.targets = this.core.isClientSide() ? (Objects.nonNull(TICKER_GC_BINARY) ?
                    (Objects.nonNull(TICKER_SA_BINARY) ?
                            Arrays.asList(HANDLER_BINARY,TICKER_BINARY,TICKER_GC_BINARY,TICKER_SA_BINARY) :
                            Arrays.asList(HANDLER_BINARY,TICKER_BINARY,TICKER_GC_BINARY)) :
                    Arrays.asList(HANDLER_BINARY,TICKER_BINARY)) : Collections.emptyList();
        TILRef.logInfo("Collecting class targets as {}",this.targets);
        return this.targets;
    }
    
    String collectTickerNames() {
        if(CoreAPI.isNamedEnv()) return "tick";
        if(this.core.getVersion().isV12()) return "func_73660_a";
        if(this.core.getVersion().isV16()) return "func_73660_a method_18669";
        return "m_120183_ method_18669"; //The rest of the versions are the same
    }
    
    String collectVolumeNames() {
        if(CoreAPI.isNamedEnv()) return "updateSourceVolume";
        if(this.core.getVersion().isV12()) return "func_184399_a";
        if(this.core.getVersion().isV16()) return "func_184399_a method_4865";
        return "m_120358_ method_4865"; //The rest of the versions are the same
    }
    
    @Override public ClassNode editClass(ClassNode classNode) {
        for(MethodNode method : classNode.methods)
            if(!volumeQuery(classNode,method,collectVolumeNames().split(" ")))
                fixMusicTicker(classNode,method,collectTickerNames().split(" "));
        return classNode;
    }
    
    /**
     * Simple check to see if the input name matches any of the other input string.
     * This is the same as Misc#equalsAny but since Misc isn't in the core package, we can't use it here.
     */
    boolean equalsAny(String name, String ... others) {
        for(String other : others)
            if(other.equals(name)) return true;
        return false;
    }
    
    public void fixMusicTicker(ClassNode classNode, MethodNode node, String ... names) {
        String className = getClassName(classNode);
        if(!this.core.getVersion().isV12() || !equalsAny(className,TICKER_NAME,TICKER_GC_NAME,TICKER_SA_NAME)) return;
        if(equalsAny(this.core.mapMethodName(classNode.name,node.name,node.desc),names)) {
            InsnList ifIns = beginList(new InsnList())
                    .insInvokeStatic(HELPER_NAME,"stopVanillaMusicTicker",TICKER_DESC)
                    .insIf(NOT_EQUAL,new Label()).insBasic(RETURN).insLabel().endList();
            ifIns.add(new FrameNode(FRAME_SAME,0,null,0,null));
            node.instructions.insertBefore(node.instructions.getFirst(),ifIns);
            TILRef.logInfo("Injected music ticker override to {}",node.name);
        }
    }
    
    @Override public String getCoreID() {
        return "musictriggers_core";
    }
    
    @Override public String getCoreName() {
        return "Music Triggers Core";
    }
    
    public boolean volumeQuery(ClassNode classNode, MethodNode node, String ... names) {
        String className = getClassName(classNode);
        if(!HANDLER_NAME.equals(className)) return false;
        if(equalsAny(this.core.mapMethodName(classNode.name,node.name,node.desc),names)) {
            node.instructions.insertBefore(node.instructions.getFirst(),new MethodInsnNode(INVOKESTATIC,HELPER_NAME,
                    "updateVolumeSources",EMPTY_DESC));
            TILRef.logInfo("Injected channel volume query to {}",node.name);
            return true;
        }
        return false;
    }
}