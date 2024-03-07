package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.List;

public class CardHelper {

    public static void parseImageCards(ChannelAPI channel, List<CardAPI> cards, List<Table> images) {
        for(Table image : images) {
            ImageElement element = new ImageElement(channel);
            if(element.parse(image)) cards.add(element);
        }
    }

    public static void parseTitleCards(ChannelAPI channel, List<CardAPI> cards, List<Table> titles) {
        for(Table title : titles) {
            TitleElement element = new TitleElement(channel);
            if(element.parse(title)) cards.add(element);
        }
    }
}