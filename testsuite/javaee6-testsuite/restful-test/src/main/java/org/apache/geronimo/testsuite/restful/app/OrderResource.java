/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.testsuite.restful.app;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/orders")
public class OrderResource {
	private static Map<Integer, Order> orders = new ConcurrentHashMap<Integer, Order>();
	private static AtomicInteger ids = new AtomicInteger();
	
	@POST
	@Consumes("application/xml")
	public Response createOrder(Order order) {
		order.setId(ids.incrementAndGet());
		
		orders.put(order.getId(), order);
		
		return Response.created(URI.create("/" + order.getId())).build();
	}
	
	@GET
	@Path("{id}")
	@Produces("application/xml")
	public Order getOrder(@PathParam("id") int id) {
		Order order =orders.get(id);
		if(order == null) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
		return order;
	}
	
	@PUT
	@Path("{id}")
	@Consumes("application/xml")
	public void updateOrder(@PathParam("id") int id, Order updateOrder) {
		Order currentOrder = orders.get(id);
		if (currentOrder == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		//currentOrder.setId(updateOrder.getId());
		currentOrder.setCustomer(updateOrder.getCustomer());
		currentOrder.setProductname(updateOrder.getProductname());
		currentOrder.setPrice(updateOrder.getPrice());
		currentOrder.setQuantity(updateOrder.getQuantity());
		currentOrder.setSeller(updateOrder.getSeller());
	}
	
	@DELETE
	@Path("{id}")
	public void deleteOrder(@PathParam("id") int id) {
		Order currentOrder =orders.get(id);
		if (currentOrder == null)
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		orders.remove(id);
	}
	
}
