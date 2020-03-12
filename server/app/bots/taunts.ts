
export enum Personality {
    kind,
    humour,
    mean,
    rand
}

export class Taunt {

    public static kind: string[] = [
        "Good work!",
        "Wow!",
        "Nice try",
        "you are good!",
        "better luck next time!",
        "you're pretty quick",
        "nicely done!"
    ];

    public static humour: string[] = [
        "q",
        "w",
        "e",
        "r",
        "t",
        "y",
    ];

    public static mean: string[] = [
        "do your best",
        "is that all you got?",
        "wake your brain cell up dude.",
        "so i hear you aren't only made up of crap",
        "you think you're so smart?",
        "they call me the fastest painter in the west",
        "it's raw!",
        "my grandmother thinks faster than you do!"
    ];

    public static getTaunts(style: Personality): string[] {
        if (style == Personality.rand)
            style = Math.floor(Math.random() * Personality.rand);

        switch (style) {
            case Personality.humour:
                return this.humour;
            case Personality.mean:
                return this.mean;
            case Personality.kind:
                return this.kind;
            default:
                return [];
        }
    }

}
