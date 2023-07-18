package sa.paylink.sdk.android.testingpaylinksdkapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import sa.paylink.sdk.android.plpaymentgateway.APIError;
import sa.paylink.sdk.android.plpaymentgateway.Environment;
import sa.paylink.sdk.android.plpaymentgateway.PaylinkGateway;
import sa.paylink.sdk.android.plpaymentgateway.model.PLPaylinkCallbackData;
import sa.paylink.sdk.android.testingpaylinksdkapplication.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private final PaylinkGateway paylinkGateway;
    private Context context;
    private Environment environment;

    public FirstFragment() {
        this.environment = Environment.TEST;

        this.paylinkGateway = new PaylinkGateway(this.environment);
        this.context = this.getContext();
    }

    private String getBackendServerUrl() {
        switch (this.environment) {
            case TEST:
                return "https://demo.paylink.sa";
            case DEV:
                return "https://frame.eu.ngrok.io";
            case PRODUCTION:
            default:
                return "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void processPayment() {
        createInvoiceInServer(new Callback<String, APIError>() {
            @Override
            public void onSuccess(String transactionNo) {
                paylinkGateway.openPaymentForm(transactionNo, context, new sa.paylink.sdk.android.plpaymentgateway.Callback<PLPaylinkCallbackData, APIError>() {
                    @Override
                    public void onSuccess(PLPaylinkCallbackData response) {
                        System.out.println("response is: " + response);
                        checkInvoiceInServer(response.getTransactionNo(), new Callback<String, APIError>() {
                            @Override
                            public void onSuccess(String orderStatus) {
                                System.out.println("order status is: " + orderStatus);
                            }

                            @Override
                            public void onError(APIError error) {
                                System.out.println("error is: " + error);
                            }
                        });
                    }

                    @Override
                    public void onError(APIError error) {
                        System.out.println("error is: " + error);
                    }
                });
            }

            @Override
            public void onError(APIError error) {
                System.out.println("API Error: " + error);
            }
        });
    }

    private void createInvoiceInServer(Callback<String, APIError> completion) {
        // Implementation for auth method, Build the URL for the auth endpoint
        String urlString = getBackendServerUrl() + "/addinvoice.php";
        // Create a JSON object with the API credentials
        JsonObjectRequest request = new JsonObjectRequest(
                urlString,
                response -> {
                    String transactionNo = null;
                    try {
                        transactionNo = response.getString("transactionNo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    completion.onSuccess(transactionNo);
                },
                error -> completion.onError(new APIError(APIError.Type.JSON_ERROR, "Error parsing auth response. " + error.getMessage()))) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Add the request to the queue to be executed
        if (context == null) {
            context = getContext();
        }
        Volley.newRequestQueue(context).add(request);
    }

    private void checkInvoiceInServer(String
                                              transactionNo, Callback<String, APIError> completion) {
        // Implementation for auth method, Build the URL for the auth endpoint
        String urlString = getBackendServerUrl() + "/getinvoice.php?transactionNo=" + transactionNo;
        // Create a JSON object with the API credentials
        JsonObjectRequest request = new JsonObjectRequest(
                urlString,
                response -> {
                    String orderStatus = null;
                    try {
                        orderStatus = response.getString("orderStatus");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    completion.onSuccess(orderStatus);
                },
                error -> completion.onError(new APIError(APIError.Type.JSON_ERROR, "Error parsing auth response. " + error.getMessage()))) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        // Add the request to the queue to be executed
        Volley.newRequestQueue(context).add(request);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonFirst.setOnClickListener(view1 -> {
            processPayment();
            System.out.println("view is: " + view1);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}