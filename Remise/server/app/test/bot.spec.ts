// import * as chai from "chai";
// import * as spies from "chai-spies";
// import { container } from "../inversify.config";
// import { Bot } from "../services/game/bots/bot";
// import { KindBot } from "../services/game/bots/kindBot";
// import { IDrawing, Format, Type } from "../interfaces/game";
// import { DisplayMode } from "../interfaces/creator";
// import { Side } from "../utils/Side";

// chai.use(spies);

// describe("Bot", () => {


//     let image: IDrawing[] = [
//         {
//             startPosX: 30,
//             startPosY: 40,
//             endPosX: 30,
//             endPosY: 40,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 31,
//             startPosY: 80,
//             endPosX: 31,
//             endPosY: 80,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 70,
//             startPosY: 81,
//             endPosX: 70,
//             endPosY: 81,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 71,
//             startPosY: 41,
//             endPosX: 71,
//             endPosY: 41,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 100,
//             startPosY: 110,
//             endPosX: 100,
//             endPosY: 110,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         }

//     ];
//     let imageCentered: IDrawing[] = [
//         {
//             startPosX: 70,
//             startPosY: 81,
//             endPosX: 70,
//             endPosY: 81,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 71,
//             startPosY: 41,
//             endPosX: 71,
//             endPosY: 41,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 31,
//             startPosY: 80,
//             endPosX: 31,
//             endPosY: 80,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 30,
//             startPosY: 40,
//             endPosX: 30,
//             endPosY: 40,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 100,
//             startPosY: 110,
//             endPosX: 100,
//             endPosY: 110,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         }

//     ];
//     let imagePanoUp: IDrawing[] = [
//         {
//             startPosX: 30,
//             startPosY: 40,
//             endPosX: 30,
//             endPosY: 40,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 71,
//             startPosY: 41,
//             endPosX: 71,
//             endPosY: 41,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 31,
//             startPosY: 80,
//             endPosX: 31,
//             endPosY: 80,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 70,
//             startPosY: 81,
//             endPosX: 70,
//             endPosY: 81,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 100,
//             startPosY: 110,
//             endPosX: 100,
//             endPosY: 110,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         }

//     ];
//     let imagePanoRight: IDrawing[] = [
//         {
//             startPosX: 100,
//             startPosY: 110,
//             endPosX: 100,
//             endPosY: 110,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 71,
//             startPosY: 41,
//             endPosX: 71,
//             endPosY: 41,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 70,
//             startPosY: 81,
//             endPosX: 70,
//             endPosY: 81,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 31,
//             startPosY: 80,
//             endPosX: 31,
//             endPosY: 80,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         },
//         {
//             startPosX: 30,
//             startPosY: 40,
//             endPosX: 30,
//             endPosY: 40,
//             color: 3,
//             width: 2,
//             isEnd: true,
//             format: Format.circle,
//             type: Type.ink
//         }

//     ];

//     let username: string = "dude";
//     let hint: string[] = ["a circle", "a blue one"];
//     let mode: DisplayMode = DisplayMode.CLASSIC;
//     let side: Side = Side.up;

//     beforeEach(() => {
//         container.snapshot();
//     });

//     afterEach(() => {
//         container.restore();
//     });

//     it("Should have the strokes in the right CLASSIC order", () => {
//         //when
//         const dude: Bot = new KindBot(image, username);
//         //then
//         chai.expect(dude).to.have.property('image').to.eql(image);
//     });

    // it("Should have the strokes in the right centered order", () => {
    //     //when
    //     const dude: Bot = new KindBot(image, username, hint, DisplayMode.CENTERED, side);

    //     //then
    //     chai.expect(dude).to.have.property('image').to.eql(imageCentered);
    // });

    // it("Should have the strokes in a panoramic from up order", () => {
    //     //when
    //     const dude: Bot = new KindBot(image, username, hint, DisplayMode.PANORAMIC, Side.up);

    //     //then
    //     chai.expect(dude).to.have.property('image').to.eql(imagePanoUp);
    // });

    // it("Should have the strokes in a panoramic from right order", () => {
    //     //when
    //     const dude: Bot = new KindBot(image, username, hint, DisplayMode.PANORAMIC, Side.right);

    //     //then
    //     chai.expect(dude).to.have.property('image').to.eql(imagePanoRight);
    // });

    // it("Should print the strokes one after the other and throw error after image.length", () => {
    //     //when
    //     const dude: Bot = new KindBot(image, username, hint, DisplayMode.CLASSIC, Side.up);
    //     //then
    //     for (let i = 0; i < dude.length; i++) {
    //         const stroke: IGameplayDraw = dude.GetNextStroke();
    //         chai.expect(stroke.username).to.equal(username);
    //         chai.expect(stroke).to.equal(image[i]);
    //     }
    //     chai.expect(dude.GetNextStroke).to.throw();
    // });

// });
