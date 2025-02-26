package mods.thecomputerizer.musictriggers.api.core;

import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.CoreEntryPoint;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
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
import static mods.thecomputerizer.theimpossiblelibrary.api.core.asm.ASMRef.*;
import static org.objectweb.asm.Type.BOOLEAN_TYPE;

@SuppressWarnings("LoggingSimilarMessage")
@MultiVersionCoreMod(modid = MODID, modName = NAME, modVersion = VERSION)
public class MTCoreEntryPoint extends CoreEntryPoint {
    
    static final String EMPTY_DESC = EMPTY_METHOD.getDescriptor();
    static final String HANDLER_BINARY = getHandlerBinary();
    static final String HANDLER_NAME = HANDLER_BINARY.replace('.','/');
    static final String HELPER_NAME = "mods/thecomputerizer/musictriggers/api/data/channel/ChannelHelper";
    static final String TICKER_BINARY = getTickerBinary();
    static final String TICKER_NAME = TICKER_BINARY.replace('.','/');
    static final String TICKER_DESC = TypeHelper.method(BOOLEAN_TYPE,new Type[]{}).getDescriptor();
    
    static String getHandlerBinary() {
        CoreAPI core = CoreAPI.getInstance();
        boolean fabric = core.getModLoader().isFabric();
        if(core.getVersion().isV12()) return "net.minecraft.client.audio.SoundHandler";
        if(core.getVersion().isV16()) return fabric ? "net.minecraft.class_1144" : "net.minecraft.client.audio.SoundHandler";
        return fabric ? "net.minecraft.class_1144" : "net.minecraft.client.sounds.SoundManager";
    }
    
    static MethodInsnNode getInvoker(String name, String desc) {
        return new MethodInsnNode(INVOKESTATIC,HELPER_NAME,name,desc);
    }
    
    static String getTickerBinary() {
        CoreAPI core = CoreAPI.getInstance();
        boolean fabric = core.getModLoader().isFabric();
        if(core.getVersion().isV12()) return "net.minecraft.client.audio.MusicTicker";
        if(core.getVersion().isV16()) return fabric ? "net.minecraft.class_1142" : "net.minecraft.client.audio.MusicTicker";
        return fabric ? "net.minecraft.class_1142" : "net.minecraft.client.sounds.MusicManager";
    }
    
    List<String> targets;
    
    public MTCoreEntryPoint() {
        TILRef.logInfo("Constructing MTCoreEntryPoint on ClassLoader {}", getClass().getClassLoader());
        TILRef.logInfo("SoundHandler name is {}",HANDLER_NAME);
        TILRef.logInfo("MusicTicker name is {}",TICKER_NAME);
    }
    
    @Override public List<String> classTargets() {
        if(Objects.isNull(this.targets)) this.targets = Arrays.asList(HANDLER_BINARY,TICKER_BINARY);
        TILRef.logInfo("Collecting class targets as {}",this.targets);
        return this.targets;
    }
    
    String collectTickerNames() {
        CoreAPI core = CoreAPI.getInstance();
        if(core.getVersion().isV12()) return "func_73660_a";
        if(core.getVersion().isV16()) return "func_73660_a method_18669";
        return "m_120183_ method_18669"; //The rest of the versions are the same
    }
    
    String collectVolumeNames() {
        CoreAPI core = CoreAPI.getInstance();
        if(core.getVersion().isV12()) return "func_184399_a";
        if(core.getVersion().isV16()) return "func_184399_a method_4865";
        return "m_120358_ method_4865"; //The rest of the versions are the same
    }
    
    @Override public ClassNode editClass(ClassNode classNode) {
        for(MethodNode method : classNode.methods)
            if(!volumeQuery(classNode,method,collectVolumeNames().split(" ")))
                fixMusicTicker(classNode,method,collectTickerNames().split(" "));
        return classNode;
    }
    
    public void fixMusicTicker(ClassNode classNode, MethodNode node, String ... names) {
        String className = getClassName(classNode);
        TILRef.logInfo("Checking music ticker: node = {} | name = {}",classNode.name,className);
        if(!TICKER_NAME.equals(className)) return;
        TILRef.logInfo("Checking music ticker inject (class = {} | method = {} | desc = {})",classNode.name,
                       node.name,node.desc);
        TILRef.logInfo("Potential method names are {}",Arrays.toString(names));
        if(Misc.equalsAny(CoreAPI.getInstance().mapMethodName(classNode.name,node.name,node.desc),names)) {
            InsnList ifIns = new InsnList();
            LabelNode skip = new LabelNode(new Label());
            ifIns.insert(getInvoker("stopVanillaMusicTicker",TICKER_DESC));
            ifIns.insert(new JumpInsnNode(NOT_EQUAL,skip));
            ifIns.insert(new InsnNode(RETURN));
            ifIns.insert(skip);
            node.instructions.insertBefore(node.instructions.getFirst(),ifIns);
            TILRef.logInfo("Injected music ticker override to {}",node.name);
        }
    }
    
    @Override public String getCoreID() {
        return MODID+"_core";
    }
    
    @Override public String getCoreName() {
        return NAME+" Core";
    }
    
    public boolean volumeQuery(ClassNode classNode, MethodNode node, String ... names) {
        String className = getClassName(classNode);
        TILRef.logInfo("Checking volume inject: node = {} | name = {}",classNode.name,className);
        if(!HANDLER_NAME.equals(className)) return false;
        TILRef.logInfo("Checking volume inject (class = {} | method = {} | desc = {})",classNode.name,
                       node.name,node.desc);
        TILRef.logInfo("Potential method names are {}",Arrays.toString(names));
        if(Misc.equalsAny(CoreAPI.getInstance().mapMethodName(classNode.name,node.name,node.desc),names)) {
            node.instructions.insertBefore(node.instructions.getFirst(),getInvoker("updateVolumeSources",EMPTY_DESC));
            TILRef.logInfo("Injected channel volume query to {}",node.name);
            return true;
        }
        return false;
    }
}