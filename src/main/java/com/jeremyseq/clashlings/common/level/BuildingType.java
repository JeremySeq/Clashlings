package com.jeremyseq.clashlings.common.level;

public enum BuildingType {
    CASTLE("/TinySwordsPack/Factions/Knights/Buildings/Castle/Castle_Blue.png",
            "/TinySwordsPack/Factions/Knights/Buildings/Castle/Castle_Construction.png",
            "/TinySwordsPack/Factions/Knights/Buildings/Castle/Castle_Destroyed.png",
            5, 4),
    HOUSE("/TinySwordsPack/Factions/Knights/Buildings/House/House_Blue.png",
            "/TinySwordsPack/Factions/Knights/Buildings/House/House_Construction.png",
            "/TinySwordsPack/Factions/Knights/Buildings/House/House_Destroyed.png",
            2, 3),
    TOWER("/TinySwordsPack/Factions/Knights/Buildings/Tower/Tower_Blue.png",
            "/TinySwordsPack/Factions/Knights/Buildings/Tower/Tower_Construction.png",
            "/TinySwordsPack/Factions/Knights/Buildings/Tower/Tower_Destroyed.png",
            2, 4),
    GOBLIN_HUT("/TinySwordsPack/Factions/Goblins/Buildings/Wood_House/Goblin_House.png",
            "/TinySwordsPack/Factions/Goblins/Buildings/Wood_House/Goblin_House.png",
            "/TinySwordsPack/Factions/Goblins/Buildings/Wood_House/Goblin_House_Destroyed.png",
            2, 3);

    public final String imageFileName;
    public final String constructionImageFileName;
    public final String destroyedImageFileName;
    public final int tileWidth;
    public final int tileHeight;

    BuildingType(String imageFileName, String constructionImageFileName, String destroyedImageFileName, int tileWidth, int tileHeight) {
        this.imageFileName = imageFileName;
        this.constructionImageFileName = constructionImageFileName;
        this.destroyedImageFileName = destroyedImageFileName;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }
}
