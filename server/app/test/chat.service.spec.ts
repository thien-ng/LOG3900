import * as chai from "chai";
import * as spies from "chai-spies";
import Types from '../types';
import { container } from "../inversify.config";
import { ChatService } from "../services/chat.service";
import { IUser } from "../interfaces/user-manager";
import { Time } from "../utils/date";

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

    it("Should add user to channelMap correctly when channel has not been added once", async () => {
        //given
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "general" }] }
        });

        //when
        await service.addUserToChannelMap({ username: "username", socketId: "socketId" });

        //then
        const list = service["channelMapUsersList"].get("general") as IUser[];
        chai.expect(list.length).to.be.equal(1);
    });

    it("Should add user to channelMap correctly when channel has been added once", async () => {
        //given
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "general" }] }
        });

        //when
        await service.addUserToChannelMap({ username: "username", socketId: "socketId" });
        await service.addUserToChannelMap({ username: "username1", socketId: "socketId1" });

        //then
        const list = service["channelMapUsersList"].get("general") as IUser[];
        chai.expect(list.length).to.be.equal(2);
    });

    it("Should remove user correctly from channel map", async () => {
        //given
        service["channelMapUsersList"].set("general", [{ username: "name", socketId: "yomama" }]);

        //when
        service.removeUserFromChannelMap("name");

        //then
        chai.expect(service["channelMapUsersList"].get("general")).to.be.equal(undefined);
    });

    it("Should not remove all user from channel map", async () => {
        //given
        service["channelMapUsersList"].set("general", [{ username: "name1", socketId: "yomama" }]);
        service["channelMapUsersList"].set("general", [{ username: "name2", socketId: "yomama" }]);

        //when
        service.removeUserFromChannelMap("name1");

        //then
        const list = service["channelMapUsersList"].get("general");
        chai.expect(list).to.be.deep.equal([{ username: "name2", socketId: "yomama" }]);
    });

    it("Should format time correctly", async () => {
        //given

        //when
        const time = Time.now();

        //then
        chai.expect(time.length).to.be.equal(8);
    });

    it("Should return error message when joining message fails", async () => {
        //given
        service["usernameMapSocketId"].set("invitee", "socketId")
        chai.spy.on(service, "joinChannel", () => { return { status: 400, message: "error while joining" } });

        //when
        const result = await service.sendInviteToChannel({ inviter: "inviter", invitee: "invitee", channel: "channel" });

        //then
        chai.expect(result.status).to.be.equal(400);
        chai.expect(result.message).to.be.equal("error while joining");
    });

    it("Should return error message when socketID not found", async () => {
        //given
        //when
        const result = await service.sendInviteToChannel({ inviter: "inviter", invitee: "invitee", channel: "channel" });

        //then
        chai.expect(result.status).to.be.equal(400);
        chai.expect(result.message).to.be.equal("cannot find invitee's socketID");
    });

});

describe("ChatService, Join/Leave", () => {

    let service: ChatService;

    beforeEach(() => {
        container.snapshot();
        service = container.get<ChatService>(Types.ChatService);
    });

    afterEach(() => {
        container.restore();
    });

    it("Should join channel succesfully", async () => {
        //given
        chai.spy.on(service["db"], "joinChannel", () => { 
            return { rows: [{ joinchannel: 0 }] }
        });
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "notEqualChannel" }] }
        });
        const spy = chai.spy.on(service, "updateUserToChannels", () => { });

        //when
        const result = await service.joinChannel({ username: "username", channel: "channel" });
        
        //then
        chai.expect(result.status).to.be.equal(200);
        chai.expect(result.message).to.be.equal("Successfully joined channel");
        chai.expect(spy).to.have.been.called.with("username", true);
    });

    it("Should return already subscribe message when joining with existing channel", async () => {
        //given
        chai.spy.on(service["db"], "joinChannel", () => { });
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "channel" }] }
        });

        //when
        const result = await service.joinChannel({ username: "username", channel: "channel" });

        //then
        chai.expect(result.status).to.be.equal(400);
        chai.expect(result.message).to.be.equal("username is already subscribed to channel.");
    });

    it("Should leave channel successfully", async () => {
        //given
        chai.spy.on(service["db"], "leaveChannel", () => { });
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "channel" }] }
        });
        const spy = chai.spy.on(service, "updateUserToChannels", () => { });

        //when
        const result = await service.leaveChannel({ username: "username", channel: "channel" });

        //then
        chai.expect(result.status).to.be.equal(200);
        chai.expect(result.message).to.be.equal("Successfully left channel");
        chai.expect(spy).to.have.been.called.with("username", false);
    });

    it("Should return is not subscribed to channel when user is not sub to channel", async () => {
        //given
        chai.spy.on(service["db"], "leaveChannel", () => { });
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "notChannel" }] }
        });

        //when
        const result = await service.leaveChannel({ username: "username", channel: "channel" });

        //then
        chai.expect(result.status).to.be.equal(400);
        chai.expect(result.message).to.be.equal("username is not subscribed to channel.");
    });

    it("Should return cannot leave default channel when leaving general", async () => {
        //given
        chai.spy.on(service["db"], "leaveChannel", () => { });
        chai.spy.on(service["db"], "getChannelsWithAccountName", () => {
            return { rows: [{ channel_id: "general" }] }
        });

        //when
        const result = await service.leaveChannel({ username: "username", channel: "general" });

        //then
        chai.expect(result.status).to.be.equal(400);
        chai.expect(result.message).to.be.equal("cannot leave default channel: general.");
    });
});
