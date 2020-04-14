import * as chai from "chai";
import * as spies from "chai-spies";
import { ArenaFfa } from "../../services/game/arena-ffa";
import { GameMode } from "../../interfaces/game";
import Types from '../../types';
import { container } from "../../inversify.config";
import { GameManagerService } from "../../services/game/game-manager.service";

chai.use(spies);

describe("Arena FFA spec", () => {

    let arena: ArenaFfa;

    beforeEach(() => {
        container.snapshot();
        const gm = container.get<GameManagerService>(Types.GameManagerService);
        arena = new ArenaFfa(GameMode.FFA, 1, [], "room", undefined, [], gm);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should sort points correctly v1", () => {
        //Given
        arena["userMapPoints"].set("user1", 2);
        arena["userMapPoints"].set("user2", 1);

        //When
        const result = arena["preparePtsToBePersisted"]();

        //Then
        const pts = [
            {username: "user1", points: 2},
            {username: "user2", points: 1},
        ];
        chai.expect(result).to.deep.equal(pts);
    });

    it("Should sort points correctly v2", () => {
        //Given
        arena["userMapPoints"].set("user1", 1);
        arena["userMapPoints"].set("user2", 2);

        //When
        const result = arena["preparePtsToBePersisted"]();

        //Then
        const pts = [
            {username: "user2", points: 2},
            {username: "user1", points: 1},
        ];
        chai.expect(result).to.deep.equal(pts);
    });

    it("Should sort points correctly v3", () => {
        //Given
        arena["userMapPoints"].set("user1", 4);
        arena["userMapPoints"].set("user2", 2);
        arena["userMapPoints"].set("user3", 3);
        arena["userMapPoints"].set("user4", 1);

        //When
        const result = arena["preparePtsToBePersisted"]();

        //Then
        const pts = [
            {username: "user1", points: 4},
            {username: "user3", points: 3},
            {username: "user2", points: 2},
            {username: "user4", points: 1},
        ];
        chai.expect(result).to.deep.equal(pts);
    });

});