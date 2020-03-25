import * as chai from "chai";
import * as spies from "chai-spies";
import { container } from "../inversify.config";
import { Bot } from "../services/game/bots/bot";
import { KindBot } from "../services/game/bots/kindBot";
import { MeanBot } from "../services/game/bots/meanBot";
import { HumourBot } from "../services/game/bots/humourBot";
import { Taunt } from "../services/game/bots/taunts";
import { IDrawing, IGameplayDraw, Format, Type } from "../interfaces/game";
import { DisplayMode } from "../interfaces/creator";
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
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
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
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
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
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 100,
            startPosY: 110,
            endPosX: 100,
            endPosY: 110,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
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
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 71,
            startPosY: 41,
            endPosX: 71,
            endPosY: 41,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 70,
            startPosY: 81,
            endPosX: 70,
            endPosY: 81,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 31,
            startPosY: 80,
            endPosX: 31,
            endPosY: 80,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        },
        {
            startPosX: 30,
            startPosY: 40,
            endPosX: 30,
            endPosY: 40,
            color: 3,
            width: 2,
            isEnd: true,
            format: Format.circle,
            type: Type.ink
        }

    ];

    let username: string = "dude";
    let hint: string[] = ["a circle", "a blue one"];
    let mode: DisplayMode = DisplayMode.CLASSIC;
    let side: Side = Side.up;

    beforeEach(() => {
        container.snapshot();
    });

    afterEach(() => {
        container.restore();
    });

    it("Should have the good default properties", () => {
        //when
        const kinddude: Bot = new KindBot(image, undefined, undefined, undefined, undefined);
        const meandude: Bot = new MeanBot(image, undefined, undefined, undefined, undefined);
        const humourdude: Bot = new HumourBot(image, undefined, undefined, undefined, undefined);
        //then
        chai.expect(kinddude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(kinddude).to.have.property('mode').to.equal(DisplayMode.CLASSIC);
        chai.expect(kinddude).to.have.property('taunts').to.eql(Taunt.kind);// eql for == instead of === cause [1,2,3] === [1,2,3] is false in typescType.inkipt.

        chai.expect(meandude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(meandude).to.have.property('mode').to.equal(DisplayMode.CLASSIC);
        chai.expect(meandude).to.have.property('taunts').to.eql(Taunt.mean);

        chai.expect(humourdude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(humourdude).to.have.property('mode').to.equal(DisplayMode.CLASSIC);
        chai.expect(humourdude).to.have.property('taunts').to.eql(Taunt.humour);
    });

    it("Should have the good properties", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, mode, side);
        //then
        chai.expect(dude).to.have.property('username').to.equal("dude");
        chai.expect(dude).to.have.property('hint').to.eql(["a circle", "a blue one"]);
        chai.expect(dude).to.have.property('mode').to.equal(DisplayMode.CLASSIC);
        chai.expect(dude).to.have.property('panoramicFirstSide').to.equal(side);
    });

    it("Should say the good hints", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, mode, side);
        //then
        let answer: string = dude.getHint();
        chai.expect(answer).to.equal("a circle");
        answer = dude.getHint();
        chai.expect(answer).to.equal("a blue one");
        answer = dude.getHint();
        chai.expect(answer).to.equal("No more hint available");
        answer = dude.getHint();
        chai.expect(answer).to.equal("No more hint available");

    });

    it("Should have the strokes in the right CLASSIC order", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, DisplayMode.CLASSIC, side);
        //then
        chai.expect(dude).to.have.property('image').to.eql(image);
    });

    it("Should have the strokes in the right centered order", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, DisplayMode.CENTERED, side);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imageCentered);
    });

    it("Should have the strokes in a panoramic from up order", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, DisplayMode.PANORAMIC, Side.up);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoUp);
    });

    it("Should have the strokes in a panoramic from right order", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, DisplayMode.PANORAMIC, Side.right);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoRight);
    });

    it("Should print the strokes one after the other and throw error after image.length", () => {
        //when
        const dude: Bot = new KindBot(image, username, hint, DisplayMode.CLASSIC, Side.up);
        //then
        for (let i = 0; i < dude.length; i++) {
            const stroke: IGameplayDraw = dude.GetNextStroke();
            chai.expect(stroke.username).to.equal(username);
            chai.expect(stroke).to.equal(image[i]);
        }
        chai.expect(dude.GetNextStroke).to.throw();
    });

});
