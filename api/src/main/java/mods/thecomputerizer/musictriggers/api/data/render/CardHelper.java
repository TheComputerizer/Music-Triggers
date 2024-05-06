package mods.thecomputerizer.musictriggers.api.data.render;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Collection;
import java.util.Objects;

public class CardHelper {

    public static void parseImageCards(ChannelAPI channel, Collection<CardAPI> cards, Toml ... images) {
        if(Objects.nonNull(images)) {
            for(Toml image : images) {
                ImageElement element = new ImageElement(channel);
                if(element.parse(image)) cards.add(element);
            }
        }
    }

    public static void parseTitleCards(ChannelAPI channel, Collection<CardAPI> cards, Toml ... titles) {
        if(Objects.nonNull(titles)) {
            for(Toml title : titles) {
                TitleElement element = new TitleElement(channel);
                if(element.parse(title)) cards.add(element);
            }
        }
    }
}