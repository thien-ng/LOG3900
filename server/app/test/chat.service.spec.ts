import * as chai from "chai";
import * as spies from "chai-spies";
import Types from '../types';
import { container } from "../inversify.config";
import { ChatService } from "../services/chat.service";

chai.use(spies);

describe("ChatService", () => {

    let service: ChatService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<ChatService>(Types.ChatService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should remove user correctly from channel map", async () => {
        // given
        service["channelMapUsersList"].set(1, [{username: "name", socketId: "yomama"}]);

        //when
        service.removeUserFromChannelMap("name");
                
        //then
        chai.expect(service["channelMapUsersList"].get(1)).to.be.equal(undefined);
    });

    it("Should not remove all user from channel map", async () => {
        // given
        service["channelMapUsersList"].set(1, [{username: "name1", socketId: "yomama"}]);
        service["channelMapUsersList"].set(1, [{username: "name2", socketId: "yomama"}]);


        //when
        service.removeUserFromChannelMap("name1");
                
        //then
        const list = service["channelMapUsersList"].get(1);
        chai.expect(list).to.be.deep.equal([{username: "name2", socketId: "yomama"}]);
    });

    it("Should format time correctly", async () => {
        // given
        //when
        const time = service["convertDateTemplate"]();
                
        //then
        chai.expect(time.length).to.be.equal(8);
    });

});

