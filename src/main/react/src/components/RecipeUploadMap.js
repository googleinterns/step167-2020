import React from "react";
import { Map, GoogleApiWrapper, Marker } from "google-maps-react";
import PropTypes from "prop-types";
import Geocode from "react-geocode";
import { mapsApiKey } from "../requests";

const RecipeUploadMap = props => {
  const displayMarker = () => {
    if (props.location) {
      return (
        <Marker
          position={{
            lat: props.location.latitude,
            lng: props.location.longitude,
          }}
          onClick={() => {
            props.setLocation(null);
            props.setDecodedRegion("");
          }}
        />
      );
    }
  };

  const updateRegion = latLng => {
    Geocode.setApiKey(mapsApiKey);
    Geocode.fromLatLng(latLng.lat(), latLng.lng()).then(
      response => {
        let temp = "";
        if (response.results[0]) {
          // If there are results, traverse the array of address components
          // looking for a country, or, if none is found, a natural feature
          response.results[0].address_components.forEach(component => {
            if (component.types.includes("country") || (component.types.includes("natural_feature") && temp === "")) {
              temp = component.long_name;
            }
          });
        }
        props.setDecodedRegion(temp);
      },
      error => {
        props.setDecodedRegion("Invalid location");
        console.log(error);
      }
    );
  };

  const mapClicked = (mapProps, map, clickEvent) => {
    const newLocation = {
      latitude: clickEvent.latLng.lat(),
      longitude: clickEvent.latLng.lng(),
    };
    props.setLocation(newLocation);
    updateRegion(clickEvent.latLng);
    map.setZoom(5);
    map.panTo(clickEvent.latLng);
  };

  return (
    <Map google={props.google} zoom={2} style={mapStyles} initialCenter={{ lat: 0, lng: 0 }} onClick={mapClicked}>
      {displayMarker()}
    </Map>
  );
};

RecipeUploadMap.propTypes = {
  location: PropTypes.object,
  setLocation: PropTypes.func,
  decodedRegion: PropTypes.string,
  setDecodedRegion: PropTypes.func,
  google: PropTypes.object,
};

const mapStyles = {
  width: "95%",
  height: "95%",
};

export default GoogleApiWrapper({
  apiKey: mapsApiKey,
})(RecipeUploadMap);
