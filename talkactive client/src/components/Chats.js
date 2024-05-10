import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux';
import SockJS from "sockjs-client";
import Stomp from "stompjs";
import "./Chats.css"
import moment from 'moment';
import { clearErrors, getAllFriends, getFriendDetails, newMessageTrueValue, newMessageFalseValue } from "../redux/actions/friendAction";
import toast from 'react-hot-toast';
import Compress from 'compress.js';
import { IoIosAttach } from "react-icons/io";
import { IoMdSend } from "react-icons/io";
import { addToCurrentChatMessages, getMessages } from '../redux/actions/messageAction';
import { CiSearch } from "react-icons/ci";
import Loader from './Loader';
import Message from './Message';
import { IS_SUBSCRIBED } from '../redux/constants/messageConstants';


const Chats = () => {

  const [isConnected, setIsConnected] = useState(false);
  const [stompClient, setStompClient] = useState(null);
  const [messageInput, setMessageInput] = useState('');
  const [subscription, setSubscription] = useState(null);
  const [searchInput, setSearchInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [image, setImage] = useState('');
  const [filteredFriends, setFilteredFriends] = useState([]);

  const dispatch = useDispatch();

  const { user } = useSelector((state) => state.user);
  const { selectedFriend, allFriends } = useSelector((state) => state.friend);
  const { chatId, error, loading } = useSelector((state) => state.message);


  useEffect(() => {
    if (error) {
      toast.error(error);
      dispatch(clearErrors());
    }
    dispatch(getAllFriends());
    connectSocket();
  }, []);


  useEffect(() => {
    setFilteredFriends(allFriends);
  }, [allFriends]);


  const connectSocket = () => {
    // const socket = new SockJS("http://192.168.2.242:8080/ws");
    const socket = new SockJS("http://localhost:8080/ws");
    // const socket = new SockJS("http://192.168.2.23:8080/ws");
    const client = Stomp.over(socket);
    setStompClient(client);
    const token = localStorage.getItem("talkactiveToken");
    const headers = {
      Authorization: `Bearer ${token}`
    };
    client.connect(headers, onConnect, onError);
  };

  const onError = (error) => {
    toast.error(error);
    console.log(error);
  }

  const onConnect = () => {
    setIsConnected(true);
  };

  useEffect(() => {
    if (isConnected && stompClient) {
      stompClient.subscribe(`/user/${user.id}/notification`, payload => onNotificationReceived(payload));
      dispatch({ type: IS_SUBSCRIBED });
    }
  }, [isConnected])

  useEffect(() => {
    if (chatId && isConnected && stompClient) {
      const sub = stompClient.subscribe(`/group/${chatId}`, (payload) => onPrivateMessageReceived(payload));
      setSubscription(sub);
    }
  }, [chatId]);

  const onNotificationReceived = (payload) => {
    const message = JSON.parse(payload.body);
    dispatch(newMessageTrueValue(message.sender));
    toast.success(`Message Received from: ${message.senderName}`)
  }

  const onPrivateMessageReceived = (payload) => {
    const message = JSON.parse(payload.body);

    if (user.id !== message.sender && message.content === "typing") {
      setIsTyping(true);
      setTimeout(() => {
        setIsTyping(false);
      }, 2000);
    }

    else if (user.id !== message.sender) {
      dispatch(addToCurrentChatMessages(message));
    }
  }


  const handleUserClick = (selectedId) => {
    if (selectedFriend && selectedFriend.id !== selectedId) {
      if (stompClient && subscription) {
        subscription.unsubscribe();
      }
      dispatch(getFriendDetails(selectedId));
      const userIds = { user1: user.id, user2: selectedId };
      dispatch(getMessages(userIds));
      dispatch(newMessageFalseValue(selectedId));
    }
  };


  const sendMessage = (e) => {
    e.preventDefault();

    let chatMessage = {};

    // if image is being sent
    if (image && stompClient) {
      chatMessage = {
        sender: user.id,
        chat: chatId,
        content: image,
        image: true,
        timestamp: moment().format('YYYY-MM-DDTHH:mm:ss'),
      };
      setImage('');
    }
    // if plain text is being sent
    else {
      chatMessage = {
        sender: user.id,
        chat: chatId,
        content: messageInput.trim(),
        image: false,
        timestamp: moment().format('YYYY-MM-DDTHH:mm:ss'),
      };
      setMessageInput('');
    }

    stompClient.send("/app/private-chat", {}, JSON.stringify(chatMessage));
    dispatch(addToCurrentChatMessages(chatMessage));

    const notificationMessage = {
      sender: user.id,
      senderName: user.name,
      recipient: selectedFriend.id,
    }
    stompClient.send(`/app/notification`, {}, JSON.stringify(notificationMessage));
  };

  const handleSearch = (value) => {
    setSearchInput(value);
    const filtered = allFriends.filter(u => u.name.match(new RegExp(value, "gi")));
    setFilteredFriends(filtered);
  }

  const handleImageUpload = async (event) => {
    const file = event.target.files[0];
    const compress = new Compress();
    const resizedImage = await compress.compress([file], {
      size: 2, // max 2 mb
      quality: 1, // values are 0 to 1
      maxWidth: 800,
      maxHeight: 800,
    })
    const img = resizedImage[0];
    const imageString = img.prefix + img.data;
    setImage(imageString);
  };

  const messageInputHandler = (val) => {
    setMessageInput(val);
    if (messageInput.trim() && stompClient) {
      const chatMessage = {
        sender: user.id,
        chat: chatId,
        content: "typing",
        image: false,
        timestamp: moment().format('YYYY-MM-DDTHH:mm:ss'),
      };
      stompClient.send("/app/typing", {}, JSON.stringify(chatMessage));
    }
  };


  return (
    <div className='chat-page'>
      <div className="chat-container">
        <div className="users-list">
          <div className="users-list-container">
            <div>
              <div className='profile-pic'><img src={user.profileImage} alt='Profile' /></div>
              {user.name}
            </div>
            <p>All Friends:</p>
            <div className='search-box'>
              <input
                type='text'
                placeholder='Search...'
                value={searchInput}
                onChange={(e) => handleSearch(e.target.value)}
              />
              <button><CiSearch /></button>
            </div>
            <ul>
              {filteredFriends && filteredFriends.map(u => (
                <li key={u.id} className={u.newMessage ? 'new-message' : 'no-message'} onClick={() => handleUserClick(u.id)}>
                  <div className='friendImage'><img src={u.profileImage} alt='Profile' /></div>
                  {u.name}
                </li>
              ))}
            </ul>
          </div>
        </div>
        {selectedFriend && selectedFriend.id ? (
          <div className="chat-area">

            <div className='selected-user-header'>
              <div className='selected-user-header-profile-image'>
                <img src={selectedFriend.profileImage} alt=''/>
              </div>
              <div className='selected-user-header-name-and-status'>
                <div>{selectedFriend && selectedFriend.name}</div>
                {isTyping ? (<div>{"Typing..."}</div>) : (<div>{selectedFriend && selectedFriend.status}</div>)}
              </div>
            </div>

            {loading ? (<Loader />) : (
              <Message />
            )}

            <div className='message-input-container'>
              <form onSubmit={e => sendMessage(e)}>

                <div className="message-input">
                  {image ? (<div className='image-message-container'><img src={image} alt=''/></div>) : (<input type="text" value={messageInput} onChange={(e) => messageInputHandler(e.target.value)} placeholder="Message..." />)}
                  <label htmlFor="image-upload" className="attach-icon">
                    <IoIosAttach />
                  </label>
                  <input id="image-upload" type="file" accept="image/*" onChange={handleImageUpload} />
                  <button type="submit"><IoMdSend /></button>
                </div>

              </form>
            </div>

          </div>
        ) : (
          <div className='empty-area'>
            <h1>Welcome to TalkActive</h1>
            <p>Where Talkative Minds Connect.</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default Chats