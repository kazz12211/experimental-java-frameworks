package workflow.integration;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;
import workflow.integration.bus.BusListener;
import workflow.integration.bus.InboundListener;
import workflow.integration.bus.OutboundListener;
import workflow.integration.channel.Channel;
import workflow.integration.channel.InputChannel;
import workflow.integration.channel.OutputChannel;
import workflow.integration.message.BusMessage;
import workflow.integration.message.MessageType;
import ariba.util.log.Log;

public class IntegrationBus implements Runnable {

	private LinkedList<BusMessage> messageQueue;
	private Thread workerThreads[];
	private boolean complete = false;
	private Map<MessageType, List<BusListener>> listeners;
	private Map<MessageType, List<OutputChannel>> outputChannels;
	private Map<MessageType, List<InputChannel>> inputChannels;
	
	public IntegrationBus(int nThreads) {
		messageQueue = new LinkedList<BusMessage>();
		workerThreads = new Thread[nThreads];
		listeners = MapUtils.map();
		outputChannels = MapUtils.map();
		inputChannels = MapUtils.map();
	}
	
	public void start() {
		int nThreads = workerThreads.length;
		Log.customer.debug("IntegrationBus creating " + nThreads + " worker threads");
		
		for(int i = 0; i < nThreads; i++) {
			workerThreads[i] = new Thread(this);
			workerThreads[i].start();
		}
		
		this.startInputChannels();
		
		Log.customer.debug("IntegrationBus started");
	}

	private void startInputChannels() {
		for(MessageType messageType : inputChannels.keySet()) {
			List<InputChannel> channels = inputChannels.get(messageType);
			for(InputChannel channel : channels) {
				channel.start();
			}
		}
	}
	
	public void stop() {
		this.destroy();
	}
	
	public void destroy() {
		complete = true;
		this.stopInputChannels();
		synchronized(messageQueue) {
			messageQueue.notifyAll();
		}
		
		Log.customer.debug("IntegrationBus stopped");
	}

	private void stopInputChannels() {
		for(MessageType messageType : inputChannels.keySet()) {
			List<InputChannel> channels = inputChannels.get(messageType);
			for(InputChannel channel : channels) {
				channel.stop();
			}
		}
	}
	
	public void post(BusMessage message) throws Exception {
		if(message.isOutgoing()) {
			List<OutputChannel> channels = this.getOutputChannels(message.getType());
			if(channels == null || channels.isEmpty())
				return;
			for(OutputChannel channel : channels) {
				if(!channel.isAsync()) {
					try {
						this.willSend(channel, message);
						channel.send(message);
						this.complete(channel, message);
					} catch (Exception e) {
						this.sendFailure(channel, message, e);
						throw e;
					}
				}
			}
		} else if(message.isIncoming()) {
			List<InputChannel> channels = this.getInputChannels(message.getType());
			if(channels == null || channels.isEmpty())
				return;
			for(InputChannel channel : channels) {
				if(!channel.isAsync()) {
					this.onMessage(channel, message);
				}
			}
		}
		
		Log.customer.debug("IntegrationBus queueing message " + message);
		synchronized(messageQueue) {
			messageQueue.addLast(message);
			messageQueue.notifyAll();
		}
	}
	
	public void sendMessage(BusMessage message) {
		MessageType type = message.getType();
		if(message.isOutgoing()) {
			List<OutputChannel> channels = this.getOutputChannels(type);
			for(OutputChannel channel : channels) {
				this.willSend(channel, message);
				try {
					channel.send(message);
					if(!channel.isAsync())
						this.complete(channel, message);
				} catch (Exception e) {
					this.sendFailure(channel, message, e);
				}
			}
		} else if(message.isIncoming()) {
			this.onMessage(null, message);
		}
	}
	
	@Override
	public void run() {
		while(!complete) {
			BusMessage message = null;
			synchronized(messageQueue) {
				if(messageQueue.size() == 0) {
					try { messageQueue.wait(); } catch (InterruptedException ignore) {}
				}
				
				if(messageQueue.size() != 0) {
					message = messageQueue.removeFirst();
				}
			}
			
			if(message != null) {
				sendMessage(message);
			}
		}
	}
	
	private void sendFailure(Channel channel, BusMessage message, Throwable error) {
		Log.customer.debug("IntegrationBus sendFailure(" + channel + ", " + message.getType() + ")");
		
		MessageType type = message.getType();
		List<OutboundListener> list = this.getOutboundListeners(type);
		for(OutboundListener listener : list) {
			listener.failed(this, channel, message, error);
		}
	}
	
	private void willSend(Channel channel, BusMessage message) {
		Log.customer.debug("IntegrationBus willSend(" + channel + ", " + message.getType() + ")");
		
		MessageType type = message.getType();
		List<OutboundListener> list = this.getOutboundListeners(type);
		for(OutboundListener listener : list) {
			listener.willSend(this, channel, message);
		}
	}
	
	private void complete(Channel channel, BusMessage message) {
		Log.customer.debug("IntegrationBus complete(" + channel + ", " + message.getType() + ")");
		
		MessageType type = message.getType();
		List<OutboundListener> list = this.getOutboundListeners(type);
		for(OutboundListener listener : list) {
			listener.complete(this, channel, message);
		}
	}
	
	private void onMessage(Channel channel, BusMessage message) {
		Log.customer.debug("IntegrationBus onMessage(" + channel + ", " + message.getType() + ")");
		
		MessageType type = message.getType();
		List<InboundListener> list = this.getInboundListeners(type);
		for(InboundListener listener : list) {
			listener.onMessage(this, channel, message);
		}
	}
	
	public void addBusListener(BusListener listener) {
		MessageType type = listener.getMessageType();
		List<BusListener> list = listeners.get(type);
		if(list == null) {
			list = ListUtils.list();
			listeners.put(type, list);
		}
		list.add(listener);
	}
	
	public void addInputChannel(InputChannel channel) {
		MessageType type = channel.getMessageType();
		List<InputChannel> channels = inputChannels.get(type);
		if(channels == null) {
			channels = ListUtils.list();
			inputChannels.put(type, channels);
		}
		channels.add(channel);
	}
	public void addOutputChannel(OutputChannel channel) {
		MessageType type = channel.getMessageType();
		List<OutputChannel> channels = outputChannels.get(type);
		if(channels == null) {
			channels = ListUtils.list();
			outputChannels.put(type, channels);
		}
		channels.add(channel);
	}
	
	private List<BusListener> getBusListenersMatchesType(String type) {
		List<BusListener> list = ListUtils.list();
		for(MessageType mt : listeners.keySet()) {
			if(mt.getType().equals(type))
				list.addAll(listeners.get(mt));
		}
		return list;
	}
	
	public List<BusListener> getBusListeners(MessageType type) {
		if(type.getSubType().equals(MessageType.ANONYMOUS_TYPE)) {
			return this.getBusListenersMatchesType(type.getType());
		} else {
			return listeners.get(type);
		}
	}
	
	public List<InboundListener> getInboundListeners(MessageType type) {
		List<BusListener> busListeners = this.getBusListeners(type);
		List<InboundListener> inbounds = ListUtils.list();
		for(BusListener listener : busListeners) {
			if(listener.isInbound())
				inbounds.add((InboundListener) listener);
		}
		return inbounds;
	}
	
	public List<OutboundListener> getOutboundListeners(MessageType type) {
		List<BusListener> busListeners = this.getBusListeners(type);
		List<OutboundListener> outbounds = ListUtils.list();
		for(BusListener listener : busListeners) {
			if(listener.isOutbound())
				outbounds.add((OutboundListener) listener);
		}
		return outbounds;
	}

	private List<OutputChannel> getOutputChannelsMatchesType(String type) {
		List<OutputChannel> list = ListUtils.list();
		for(MessageType mt : outputChannels.keySet()) {
			if(mt.getType().equals(type)) {
				list.addAll(outputChannels.get(mt));
			}
		}
		return list;
	}

	private List<InputChannel> getInputChannelsMatchesType(String type) {
		List<InputChannel> list = ListUtils.list();
		for(MessageType mt : inputChannels.keySet()) {
			if(mt.getType().equals(type)) {
				list.addAll(inputChannels.get(mt));
			}
		}
		return list;
	}
	
	public List<OutputChannel> getOutputChannels(MessageType type) {
		if(type.getSubType().equals(MessageType.ANONYMOUS_TYPE)) {
			return getOutputChannelsMatchesType(type.getType());
		} else {
			return outputChannels.get(type);
		}
	}
	
	public List<InputChannel> getInputChannels(MessageType type) {
		if(type.getSubType().equals(MessageType.ANONYMOUS_TYPE)) {
			return getInputChannelsMatchesType(type.getType());
		} else {
			return inputChannels.get(type);
		}
	}
}
