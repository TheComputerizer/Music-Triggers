package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioHelper;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.command.CommandElement;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.CardAPI;
import mods.thecomputerizer.musictriggers.api.data.render.CardHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class ChannelData extends ChannelElement {

    private final List<AudioRef> audio;
    private final List<CardAPI> cards;
    private final List<CommandElement> commands;
    private final List<RecordElement> records;
    private final List<RedirectElement> redirects;
    private final List<TriggerAPI> triggers;

    public ChannelData(ChannelAPI channel) {
        super(channel);
        this.audio = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.commands = new ArrayList<>();
        this.records = new ArrayList<>();
        this.redirects = new ArrayList<>();
        this.triggers = new ArrayList<>();
    }

    public void clear() {
        this.audio.clear();
        this.cards.clear();
        this.commands.clear();
        this.records.clear();
        this.redirects.clear();
        this.triggers.clear();
    }

    public void readCommands(@Nullable Holder commands) {
        if(Objects.isNull(commands)) return;
        for(Table table : commands.getTables().values()) {
            CommandElement command = new CommandElement(getChannel(),table);
            if(command.isValid()) this.commands.add(command);
        }
    }

    public void readJukebox(List<String> lines) {
        for(String line : lines) {
            RecordElement record = new RecordElement(getChannel(),line);
            if(record.isValid()) this.records.add(record);
        }
    }

    public void readMain(@Nullable Holder main) {
        if(Objects.isNull(main)) return;
        TriggerHelper.parseTriggers(getChannel(),this.triggers,main.getTableByName("triggers"));
        AudioHelper.parseAudio(getChannel(),this.audio,main.getTableByName("songs"));
    }

    public void readRedirect(List<String> lines) {
        for(String line : lines) {
            RedirectElement redirect = new RedirectElement(getChannel(),line);
            if(redirect.isValid()) this.redirects.add(redirect);
        }
    }

    public void readRenders(@Nullable Holder renders) {
        if(Objects.isNull(renders)) return;
        CardHelper.parseImageCards(getChannel(),this.cards,renders.getTablesByName("image"));
        CardHelper.parseTitleCards(getChannel(),this.cards,renders.getTablesByName("title"));
    }

    public void parse() {
        readRedirect(ChannelHelper.openTxt(getChannel().getInfo().getRedirectPath(),getChannel()));
        readMain(ChannelHelper.openToml(getChannel().getInfo().getMainPath(),getChannel()));
        readRenders(ChannelHelper.openToml(getChannel().getInfo().getRendersPath(),getChannel()));
        readCommands(ChannelHelper.openToml(getChannel().getInfo().getCommandsPath(),getChannel()));
        readJukebox(ChannelHelper.openTxt(getChannel().getInfo().getJukeboxPath(),getChannel()));
    }
}