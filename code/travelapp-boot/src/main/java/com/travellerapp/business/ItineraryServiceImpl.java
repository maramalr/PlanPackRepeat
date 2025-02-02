package com.travellerapp.business;

import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.travellerapp.domain.Destination;
import com.travellerapp.domain.Itinerary;
import com.travellerapp.repositories.DestinationRepository;
import com.travellerapp.repositories.ItineraryRepository;


@Service
public class ItineraryServiceImpl implements ItineraryService{

	@Autowired
	private ItineraryRepository itineraryRepo;
	
	@Autowired
	private DestinationRepository destRepo;
	
	
	@Override
	public List<Itinerary> listAllItineraries() {
		return (List<Itinerary>) itineraryRepo.findAll();
	}
	
	@Override
	public Itinerary getActiveItineraryByEmail(String email) {
		return itineraryRepo.findItineraryByEmail(email);
	}

	@Override
	public Itinerary createItinerary(Itinerary itinerary) {
		Itinerary currentIt=itineraryRepo.findItineraryByEmail(itinerary.getEmail());
		itinerary.getDestinations().forEach(x->{
			Destination des= destRepo.save(x);
			x= des; 
		});
		if(currentIt!= null) {
			return updateItinerary(new ObjectId(currentIt.getId()), itinerary);
		}
		return itineraryRepo.save(itinerary);
	}
	
	@Override
	public Itinerary updateItinerary(ObjectId id, Itinerary itinerary) {
		itinerary.setId(id);
		return itineraryRepo.save(itinerary);
	}
	
	@Override
	public void deleteItinerary(ObjectId id) {
		Itinerary itr= itineraryRepo.findItineraryBy_id(id);
		if(itr!=null) {
			destRepo.deleteAll(itr.getDestinations());
			itineraryRepo.delete(itr);
		}
	}
	
	
	@Override
	public List<Itinerary> createItineraries(List<Itinerary> itinerary) {
		return itineraryRepo.saveAll(itinerary);
	}
	
	@Override 
	public void deleteDestinationFromItinerary(String email,List<String> destinationIds) {
		//get current itinerary based on email 
		Itinerary itr= getActiveItineraryByEmail(email);
		if(itr!=null)
		{
			List<Destination> destinations= itr.getDestinations();
			//getCurrentDestId's
			List<String> existingDest= destinations.stream().map(Destination::getId).collect(Collectors.toList());
			//check what can be deleted from existing
			List<String> toBeDeletedDest = existingDest.stream()
	                .filter(destinationIds::contains)
	                .collect(Collectors.toList());
			//if toBeDeletedDest is not empty , then go ahead and delete it
			if(!CollectionUtils.isEmpty(toBeDeletedDest)) {
				toBeDeletedDest.forEach(x->destRepo.deleteById(x));
			}
			//delink the deleteddest from itinerary
			List<Destination> updateActiveDest=  destinations.stream().filter(x->!toBeDeletedDest.contains(x.getId())).collect(Collectors.toList());
			//update Active destination
			itr.setDestinations(updateActiveDest);
			updateItinerary(new ObjectId(itr.getId()), itr);
		}
		
		
	}
	
}
