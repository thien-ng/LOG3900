import * as chai from "chai";
import * as spies from "chai-spies";
import { container } from "../inversify.config";
import { Bot } from "../bots/bot";
import { kindBot } from "../bots/kindBot";
import { meanBot } from "../bots/meanBot";
import { humourBot } from "../bots/humourBot";
import { Taunt } from "../bots/taunts";
import { IDrawing, IGameplayDraw } from "../interfaces/game";
import { DisplayMode } from "../bots/taunts";
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
    let side: Side = Side.up;

    beforeEach(() => {
        container.snapshot();
    });

    afterEach(() => {
        container.restore();
    });

    it("Should have the good default properties", () => {
        //when
        const kinddude: Bot = new kindBot(image, undefined, undefined, undefined, undefined);
        const meandude: Bot = new meanBot(image, undefined, undefined, undefined, undefined);
        const humourdude: Bot = new humourBot(image, undefined, undefined, undefined, undefined);
        //then
        chai.expect(kinddude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(kinddude).to.have.property('hint').to.equal("no hint for you!");
        chai.expect(kinddude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(kinddude).to.have.property('taunts').to.eql(Taunt.kind);// eql for == instead of === cause [1,2,3] === [1,2,3] is false in typescript.

        chai.expect(meandude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(meandude).to.have.property('hint').to.equal("no hint for you!");
        chai.expect(meandude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(meandude).to.have.property('taunts').to.eql(Taunt.mean);

        chai.expect(humourdude).to.have.property('username').to.equal("BOT:bob");
        chai.expect(humourdude).to.have.property('hint').to.equal("no hint for you!");
        chai.expect(humourdude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(humourdude).to.have.property('taunts').to.eql(Taunt.humour);
    });

    it("Should have the good properties", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, mode, side);
        //then
        chai.expect(dude).to.have.property('username').to.equal("dude");
        chai.expect(dude).to.have.property('hint').to.equal("a circle");
        chai.expect(dude).to.have.property('mode').to.equal(DisplayMode.classic);
        chai.expect(dude).to.have.property('panoramicFirstSide').to.equal(side);
    });

    it("Should have the strokes in the right classic order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.classic, side);
        //then
        chai.expect(dude).to.have.property('image').to.eql(image);
    });

    it("Should have the strokes in the right centered order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.centered, side);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imageCentered);
    });

    it("Should have the strokes in a panoramic from up order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.panoramic, Side.up);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoUp);
    });

    it("Should have the strokes in a panoramic from right order", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.panoramic, Side.right);

        //then
        chai.expect(dude).to.have.property('image').to.eql(imagePanoRight);
    });

    it("Should print the strokes one after the other and throw error after image.length", () => {
        //when
        const dude: Bot = new Bot(image, username, hint, DisplayMode.classic, Side.up);
        //then
        for (let i = 0; i < dude.length; i++) {
            const stroke: IGameplayDraw = dude.GetNextStroke();
            chai.expect(stroke.username).to.equal(username);
            chai.expect(stroke).to.equal(image[i]);
        }
        chai.expect(dude.GetNextStroke).to.throw();
    });

});
