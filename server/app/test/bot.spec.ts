import * as chai from "chai";
import * as spies from "chai-spies";
import { container } from "../inversify.config";
import { Bot } from "../bots/bot";
import { IDrawing, IGameplayDraw } from "../interfaces/game";
import { DisplayMode, Personality } from "../bots/taunts";
import { Side } from "../utils/Side";

chai.use(spies);

describe("Bot", () => {


    let image: IDrawing[] = [
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
        }

    ];
    let imageCentered: IDrawing[] = [
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
        },
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
        }

    ];
    let imagePanoUp: IDrawing[] = [
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
        }

    ];
    let imagePanoRight: IDrawing[] = [
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
        },
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
        }

    ];

    let username: string = "dude";
    let hint: string = "a circle";
    let mode: DisplayMode = DisplayMode.classic;
    let style: Personality = Personality.kind;
    let side: Side = Side.up;

    beforeEach(() => {
        container.snapshot();
    });

    afterEach(() => {
        container.restore();
    });

    it("Should have the good default properties", () => {
        //when
        const dude: Bot = new Bot(image, undefined, undefined, undefined, undefined, undefined);
        //then
        chai.expect(dude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(dude).to.have.property('hint').to.equal("no hint for you!");
        chai.expect(dude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(dude).to.have.property('taunts').not.to.equal(undefined);
    });

    it("Should have the good properties", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, mode, style, side);
        //then
        chai.expect(dude).to.have.property('username').to.equal("dude");
        chai.expect(dude).to.have.property('hint').to.equal("a circle");
        chai.expect(dude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(dude).to.have.property('panoramicFirstSide').to.equal(side);
        chai.expect(dude).to.have.property('taunts').to.eql([ // eql for == instead of === cause [1,2,3] === [1,2,3] is false in typescript.
            "Good work!",
            "Wow!",
            "Nice try",
            "you are good!",
            "better luck next time!",
            "you're pretty quick",
            "nicely done!" // those are the taunts of the kind personality
        ]);
    });

    it("Should have the strokes in the right classic order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.classic, style, side);
        //then
        chai.expect(dude).to.have.property('image').to.eql(image);
    });

    it("Should have the strokes in the right centered order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.centered, style, side);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imageCentered);
    });

    it("Should have the strokes in a panoramic from up order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.panoramic, style, Side.up);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoUp);
    });

    it("Should have the strokes in a panoramic from right order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.panoramic, style, Side.right);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoRight);
    });

    it("Should print the strokes one after the other and throw error after image.length", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.classic, style, Side.up);
        //then
        for (let i = 0; i < dude.length; i++) {
            const stroke: IGameplayDraw = dude.GetNextStroke();
            chai.expect(stroke.username).to.equal(username);
            chai.expect(stroke).to.equal(image[i]);
        }
        chai.expect(dude.GetNextStroke).to.throw();
    });

});
