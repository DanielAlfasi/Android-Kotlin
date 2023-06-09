package com.example.archtectureproject.ui.addchore


import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.archtectureproject.ui.ChoreViewModel
import com.example.archtectureproject.R
import com.example.archtectureproject.data.model.Chore
import com.example.archtectureproject.databinding.AddChoreLayoutBinding
import com.example.archtectureproject.ui.UserViewModel
import java.util.*
import androidx.lifecycle.Observer
import com.example.archtectureproject.data.model.User

class AddChoreFragment : Fragment() {

    private val viewModel: ChoreViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private var _binding: AddChoreLayoutBinding? = null
    private val binding get() = _binding!!
    private var date: Long = 0L
    private lateinit var userSpinner: Spinner
    private var selectedUserId = -1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AddChoreLayoutBinding.inflate(inflater, container, false)

        binding.pickDateBtn.setOnClickListener {
            dateBtnClicked()
        }

        userSpinner =  binding.userPickSpinner

        val allUsers = userViewModel.users

        allUsers.observe(viewLifecycleOwner, Observer { userList ->
            allUsers.value
            val userNames = userList.map { "${it.firstName} ${it.lastName}" }
            val arrayAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                userNames
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            userSpinner.adapter = arrayAdapter
            handleUserPick(allUsers.value)
        })

        binding.finishBtn.setOnClickListener {
            finishBtnClicked()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.go_to_home -> {
                        findNavController().navigate(R.id.action_addChoreFragment_to_homeFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    private fun finishBtnClicked() {

        if (date == 0L) {
            val builder = AlertDialog.Builder(requireContext())

            builder.setTitle(getString(R.string.date_not_picked_title))
            builder.setMessage(getString(R.string.date_not_picked_desc))
            builder.setPositiveButton(getString(R.string.date_not_picked_ok)) { dialog, _ ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()

            alertDialog.show()
        }
        else if (binding.choreTitle.text.isNullOrBlank() and binding.choreReward.text.isNullOrBlank()) {
            binding.choreTitle.error = getString(R.string.field_not_null_error)
            binding.choreReward.error = getString(R.string.field_not_null_error)
        }
        else if (binding.choreTitle.text.isNullOrBlank()) {
            binding.choreTitle.error = getString(R.string.field_not_null_error)
        }
        else if (binding.choreReward.text.isNullOrBlank()) {
            binding.choreReward.error = getString(R.string.field_not_null_error)
        }
        else {
            val chore = Chore(
                binding.choreTitle.text.toString(),
                binding.choreDescription.text.toString(),
                binding.choreReward.text.toString().toInt(),
                date,
                selectedUserId
            )

            viewModel.addChore(chore)

            findNavController().navigate(R.id.action_addChoreFragment_to_allChoresFragment)
        }

    }

    private fun dateBtnClicked() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                // Create a Calendar object with the picked date
                val pickedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }

                // Convert the picked date to a Date object
                date = pickedDate.timeInMillis
                pickedDate.set(Calendar.HOUR_OF_DAY, 0)
                pickedDate.set(Calendar.MINUTE, 0)
                pickedDate.set(Calendar.SECOND, 0)
                pickedDate.set(Calendar.MILLISECOND, 0)

                // show the picked date on screen
                val selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding.dateText.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // set the minimum date to current date
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    // handle user assigned to chore
    private fun handleUserPick(userList: List<User>?) {
        userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedUser = userList!![position]
                selectedUserId = selectedUser.id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}