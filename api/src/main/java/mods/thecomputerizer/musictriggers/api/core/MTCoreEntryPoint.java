package mods.thecomputerizer.musictriggers.api.core;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.MultiVersionCoreMod;
import mods.thecomputerizer.theimpossiblelibrary.api.core.asm.TypeHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.musictriggers.api.MTRef.NAME;
import static mods.thecomputerizer.musictriggers.api.MTRef.VERSION;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.TILDev.DEV;
import static mods.thecomputerizer.theimpossiblelibrary.api.core.asm.ASMRef.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;

@MultiVersionCoreMod(modid = MODID, modName = NAME, modVersion = VERSION)
public class MTCoreEntryPoint extends CoreEntryPoint {
    
    static final String EMPTY_DESC = EMPTY_METHOD.getDescriptor();
    static final String HELPER_NAME = "mods/thecomputerizer/musictriggers/api/data/channel/ChannelHelper";
    static final String TICKER_DESC = TypeHelper.method(BOOLEAN_TYPE,new Type[]{}).getDescriptor();
    
    static MethodInsnNode getInvoker(String name, String desc) {
        return new MethodInsnNode(INVOKESTATIC,HELPER_NAME,name,desc);
    }
    
    List<String> targets;
    
    public MTCoreEntryPoint() {
        MTRef.logDebug("Constructing MTCoreEntryPoint on ClassLoader {}",getClass().getClassLoader());
    }
    
    @Override public List<String> classTargets() {
        if(Objects.isNull(this.targets)) {
            CoreAPI core = CoreAPI.getInstance();
            boolean fabric = core.getModLoader().isFabric();
            switch(core.getVersion()) {
                case V12_2: {
                    this.targets = Arrays.asList("net.minecraft.client.audio.MusicTicker","net.minecraft.client.audio.SoundHandler");
                    break;
                }
                case V16_5: {
                    String soundHandler = "net.minecraft."+(fabric ? "class_1144" : "client.audio.SoundHandler");
                    String musicTicker = "net.minecraft."+(fabric ? "class_1142" : "client.audio.MusicTicker");
                    this.targets = Arrays.asList(soundHandler,musicTicker);
                    break;
                }
            }
        }
        return this.targets;
    }
    
    String[] collectTickerNames() {
        CoreAPI core = CoreAPI.getInstance();
        switch(core.getVersion()) {
            case V12_2: return new String[]{"update","func_73660_a"};
            case V16_5: return new String[]{"tick","func_73660_a","method_18669"};
        }
        return new String[]{};
    }
    
    String[] collectVolumeNames() {
        CoreAPI core = CoreAPI.getInstance();
        switch(core.getVersion()) {
            case V12_2: return new String[]{"setSoundLevel","func_184399_a"};
            case V16_5: return new String[]{"updateSourceVolume","func_184399_a","method_4865"};
        }
        return new String[]{};
    }
    
    @Override public ClassNode editClass(ClassNode classNode) {
        for(MethodNode method : classNode.methods)
            if(!volumeQuery(classNode,method,collectVolumeNames()) && (!DEV || classNode.name.contains("Music")))
                fixMusicTicker(classNode,method,collectTickerNames());
        return classNode;
    }
    
    public void fixMusicTicker(ClassNode classNode, MethodNode node, String ... names) {
        if(Misc.equalsAny(getMethodName(classNode,node),names)) {
            InsnList ifIns = new InsnList();
            LabelNode skip = new LabelNode(new Label());
            ifIns.insert(getInvoker("stopVanillaMusicTicker",TICKER_DESC));
            ifIns.insert(new JumpInsnNode(NOT_EQUAL,skip));
            ifIns.insert(new InsnNode(RETURN));
            ifIns.insert(skip);
            node.instructions.insertBefore(node.instructions.getFirst(),ifIns);
        }
    }
    
    @Override public String getCoreID() {
        return MODID+"_core";
    }
    
    @Override public String getCoreName() {
        return NAME+" Core";
    }
    
    public boolean volumeQuery(ClassNode classNode, MethodNode node, String ... names) {
        String name = getMethodName(classNode,node);
        MTRef.logInfo("volumeQuery is checking method ({})",name);
        if(Misc.equalsAny(name,names)) {
            node.instructions.insertBefore(node.instructions.getFirst(),getInvoker("updateVolumeSources",EMPTY_DESC));
            MTRef.logInfo("Injected channel volume query to {}",name);
        }
        return false;
    }
}