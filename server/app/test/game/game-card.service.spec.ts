import * as chai from "chai";
import * as spies from "chai-spies";

import Types from '../../types';
import { container } from "../../inversify.config";
import { GameCardService } from "../../services/game/game-card.service";

chai.use(spies);

describe("Game Card Service Test", () => {

    let service: GameCardService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<GameCardService>(Types.GameCardService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should not delete a card when lobby is not found", async () => {
        //given
        chai.spy.on(service["lobServ"], "getActiveLobbies", () => {return []});
        const spy = chai.spy.on(service["db"], "deleteCard", () => {});

        //when
        //then
        try { service.deleteCard("gameID") }
        catch(e) {chai.expect(e.message).to.equal("Some lobbies are still active with this game card")}
        chai.expect(spy).to.not.have.been.called;
    });

    it("Should delete a card when lobby is found", async () => {
        //given
        chai.spy.on(service["lobServ"], "getActiveLobbies", () => {return ["aGame"]});
        const spy = chai.spy.on(service["db"], "deleteCard", () => {});

        //when
        service.deleteCard("gameID");

        //then
        chai.expect(spy).to.have.been.called;
    });

});