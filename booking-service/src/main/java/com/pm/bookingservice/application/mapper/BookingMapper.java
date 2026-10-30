package com.pm.bookingservice.application.mapper;

import com.pm.bookingservice.application.dto.request.BookingAddressRequest;
import com.pm.bookingservice.application.dto.response.BookingAddressResponse;
import com.pm.bookingservice.application.dto.response.BookingResponse;
import com.pm.bookingservice.domain.model.Booking;
import com.pm.bookingservice.domain.model.BookingAddress;


public class BookingMapper {
    private BookingMapper() {

    }


    public static BookingAddress toAddress(
            BookingAddressRequest request
    ) {

        return new BookingAddress(

                request.addressLine1(),

                request.addressLine2(),

                request.city(),

                request.state(),

                request.postalCode(),

                request.country()

        );
    }



    public static BookingAddressResponse toResponse(
            BookingAddress address
    ) {

        return new BookingAddressResponse(

                address.getAddressLine1(),

                address.getAddressLine2(),

                address.getCity(),

                address.getState(),

                address.getPostalCode(),

                address.getCountry()

        );
    }




    public static BookingResponse toResponse(
            Booking booking
    ) {

        return new BookingResponse(

                booking.getId(),

                booking.getUserId(),

                booking.getProviderUserId(),

                booking.getProviderServiceId(),

                booking.getServiceName(),

                booking.getDescription(),

                booking.getPrice(),

                toResponse(booking.getAddress()),

                booking.getScheduledStart(),

                booking.getScheduledEnd(),

                booking.getStatus(),

                booking.getNotes(),

                booking.getCreatedAt()

        );

    }

}
